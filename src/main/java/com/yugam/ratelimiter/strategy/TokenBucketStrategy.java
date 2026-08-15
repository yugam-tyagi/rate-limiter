package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.clientState.TokenBucketClientInfo;
import com.yugam.ratelimiter.model.policy.TokenBucketPolicy;
import com.yugam.ratelimiter.model.state.TokenBucketState;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;

@Component
public class TokenBucketStrategy implements RateLimiterStrategy<TokenBucketState, TokenBucketPolicy>{
    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.TOKEN_BUCKET;
    }

    @Override
    public RateLimitResponse processRequest(TokenBucketState state, TokenBucketPolicy policy, Instant now) {
        int bucketCapacity = policy.getBucketCapacity();
        int refillRate = policy.getRefillRate();
        int currentCapacity = state.getAvailableTokens();
        Instant lastRefillTime = state.getLastRefillTime();
        int elapsedTime;
        int generatedTokens = 0;

        if(lastRefillTime==null){
            currentCapacity=bucketCapacity;
            lastRefillTime=now;
        }
        else{
            elapsedTime = Math.toIntExact(Duration.between(lastRefillTime, now).toMinutes());
            generatedTokens = refillRate*elapsedTime;
            currentCapacity = Math.min(bucketCapacity,currentCapacity+generatedTokens);
        }

        if(currentCapacity==0){
            throw new RateLimitExceededException((long) Math.ceil(60.0 / refillRate));
        }

        currentCapacity--;

        if(generatedTokens>0){
            lastRefillTime=now;
        }

        state.setAvailableTokens(currentCapacity);
        state.setLastRefillTime(lastRefillTime);

        return new RateLimitResponse(true,currentCapacity);
    }
}
