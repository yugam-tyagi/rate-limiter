package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;
import com.yugam.ratelimiter.model.TokenBucketClientInfo;
import com.yugam.ratelimiter.model.TokenBucketPolicy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class TokenBucketStrategy implements RateLimiterStrategy<TokenBucketClientInfo, TokenBucketPolicy>{
    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.TOKEN_BUCKET;
    }

    @Override
    public RateLimitResponse processRequest(TokenBucketClientInfo clientRequestInfo, TokenBucketPolicy policy, Instant now) {
        synchronized (clientRequestInfo){
            int bucketCapacity = policy.getBucketCapacity();
            int refillRate = policy.getRefillRate();
            int currentCapacity = clientRequestInfo.getAvailableTokens();
            Instant lastRefillTime = clientRequestInfo.getLastRefillTime();
            int elapsedTime = Math.toIntExact(Duration.between(lastRefillTime, now).toSeconds());
            int generatedTokens = refillRate*elapsedTime;

            currentCapacity = Math.min(bucketCapacity,currentCapacity+generatedTokens);

            if(currentCapacity==0){
                throw new RateLimitExceededException(1);
            }

            currentCapacity--;

            if(generatedTokens>0){
                lastRefillTime=now;
            }
            clientRequestInfo.refreshTokensAndRefillTime(currentCapacity,lastRefillTime);

            return new RateLimitResponse(true,currentCapacity);
        }
    }
}
