package com.yugam.ratelimiter.model.policy;

import com.yugam.ratelimiter.enums.AlgorithmType;

public interface RateLimitPolicy {
    AlgorithmType getAlgorithmType();
}
