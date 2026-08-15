package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.policy.LeakyBucketPolicy;
import com.yugam.ratelimiter.model.state.LeakyBucketState;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;

@Component
public class LeakyBucketStrategy implements RateLimiterStrategy<LeakyBucketState, LeakyBucketPolicy>{
    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.LEAKY_BUCKET;
    }

    @Override
    public RateLimitResponse processRequest(LeakyBucketState state, LeakyBucketPolicy policy, Instant now) {
        int bucketCapacity = policy.getBucketCapacity();
        int leakRate = policy.getLeakRate();
        int currentRequestCount = state.getCurrentRequestCount();
        Instant lastLeakTime = state.getLastLeakTime();
        int elapsedTime;

        if(lastLeakTime==null){
            elapsedTime = 0;
            currentRequestCount=0;
        }
        else{
            elapsedTime = Math.toIntExact(Duration.between(lastLeakTime,now).toMinutes());
            currentRequestCount = Math.max(0,currentRequestCount-leakRate*elapsedTime);
        }

        if(currentRequestCount>=bucketCapacity){
            throw new RateLimitExceededException((long) Math.ceil(60.0 / leakRate));
        }

        currentRequestCount++;

        if (lastLeakTime == null) {
            lastLeakTime = now;
        } else {
            lastLeakTime = lastLeakTime.plusSeconds(elapsedTime * 60L);
        }

        state.setCurrentRequestCount(currentRequestCount);
        state.setLastLeakTime(lastLeakTime);

        return new RateLimitResponse(true,bucketCapacity-currentRequestCount);
    }
}
