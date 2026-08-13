package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;

import java.time.Instant;

public interface RateLimiterStrategy<C,P> {
    AlgorithmType getAlgorithmType();
    RateLimitResponse processRequest(C clientRequestInfo, P policy, Instant instant);
}
