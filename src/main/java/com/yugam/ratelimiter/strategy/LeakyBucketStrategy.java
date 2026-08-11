package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.LeakyBucketClientInfo;
import com.yugam.ratelimiter.model.LeakyBucketPolicy;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;

@Component
public class LeakyBucketStrategy implements RateLimiterStrategy<LeakyBucketClientInfo, LeakyBucketPolicy>{
    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.LEAKY_BUCKET;
    }

    @Override
    public RateLimitResponse processRequest(LeakyBucketClientInfo clientRequestInfo, LeakyBucketPolicy policy, Instant now) {
        synchronized (clientRequestInfo){
            int bucketCapacity = policy.getBucketCapacity();
            int leakRate = policy.getLeakRate();
            int currentRequestCount = clientRequestInfo.getCurrentRequestCount();
            Instant lastLeakTime = clientRequestInfo.getLastLeakTime();
            int elapsedTime = Math.toIntExact(Duration.between(lastLeakTime,now).toMinutes());

            currentRequestCount = Math.max(0,currentRequestCount-leakRate*elapsedTime);

            if(currentRequestCount>=bucketCapacity){
                throw new RateLimitExceededException(60);
            }

            currentRequestCount++;
            lastLeakTime = now;

            clientRequestInfo.refreshRequestCountAndLeakTime(currentRequestCount,lastLeakTime);

            return new RateLimitResponse(true,bucketCapacity-currentRequestCount);
        }
    }
}
