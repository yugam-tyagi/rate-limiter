package com.yugam.ratelimiter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LeakyBucketPolicy implements RateLimitPolicy{
    private final int bucketCapacity;
    private final int leakRate; //Unit: requests/minute
}
