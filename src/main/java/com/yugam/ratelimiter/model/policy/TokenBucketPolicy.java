package com.yugam.ratelimiter.model.policy;

import com.yugam.ratelimiter.enums.AlgorithmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenBucketPolicy implements RateLimitPolicy{
    private final int bucketCapacity;
    private final int refillRate; //Unit: tokens/minute

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.TOKEN_BUCKET;
    }
}
