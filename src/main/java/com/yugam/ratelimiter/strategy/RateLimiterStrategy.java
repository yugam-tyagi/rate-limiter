package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;

import java.time.Instant;

public interface RateLimiterStrategy {
    AlgorithmType getAlgorithmType();
    RateLimitResponse processRequest(ClientRequestInfo clientRequestInfo, RateLimitPolicy policy, Instant instant);
}
