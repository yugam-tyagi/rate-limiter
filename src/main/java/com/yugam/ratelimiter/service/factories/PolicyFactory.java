package com.yugam.ratelimiter.service.factories;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.PolicyData;
import com.yugam.ratelimiter.model.policy.*;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class PolicyFactory {
    public RateLimitPolicy getPolicy(AlgorithmType algorithmType, PolicyData data){
        RateLimitPolicy policy = null;
        int maxRequests;
        int bucketCapacity;
        int refillRate;
        int leakRate;
        long windowSeconds;
        Duration windowDuration;
        switch (algorithmType){
            case FIXED_WINDOW:
                maxRequests = ((Number) data.getData().get("maxRequests")).intValue();
                windowSeconds = ((Number) data.getData().get("windowDuration")).longValue();
                windowDuration = Duration.ofSeconds(windowSeconds);
                policy = new FixedWindowPolicy(maxRequests, windowDuration);
                break;

            case SLIDING_WINDOW:
                maxRequests = ((Number) data.getData().get("maxRequests")).intValue();
                windowSeconds = ((Number) data.getData().get("windowDuration")).longValue();
                windowDuration = Duration.ofSeconds(windowSeconds);
                policy = new SlidingWindowPolicy(maxRequests, windowDuration);
                break;

            case TOKEN_BUCKET:
                bucketCapacity = ((Number) data.getData().get("bucketCapacity")).intValue();
                refillRate = ((Number) data.getData().get("refillRate")).intValue();
                policy = new TokenBucketPolicy(bucketCapacity,refillRate);
                break;

            case LEAKY_BUCKET:
                bucketCapacity = ((Number) data.getData().get("bucketCapacity")).intValue();
                leakRate = ((Number) data.getData().get("leakRate")).intValue();
                policy = new LeakyBucketPolicy(bucketCapacity,leakRate);
                break;
        }
        return policy;
    }
}
