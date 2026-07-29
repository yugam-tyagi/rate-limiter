package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;

import java.time.Instant;

public interface RateLimiterStrategy {
    AlgorithmType getAlgorithmType();
    boolean processRequest(ClientRequestInfo clientRequestInfo, RateLimitPolicy policy, Instant instant);
}
