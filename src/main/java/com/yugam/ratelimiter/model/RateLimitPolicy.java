package com.yugam.ratelimiter.model;

import com.yugam.ratelimiter.enums.AlgorithmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
@Builder
@AllArgsConstructor
public class RateLimitPolicy {
    private final int maxRequests;
    private final Duration windowDuration;
    private final AlgorithmType algorithm;
    private final int refillRate; //Unit: tokens/second
}
