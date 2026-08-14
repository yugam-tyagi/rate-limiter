package com.yugam.ratelimiter.repository;

import com.yugam.ratelimiter.enums.AlgorithmType;

public interface RedisRateLimitStateRepository<S> {
    public AlgorithmType getAlgorithmType();
    S getState(String clientId);
    void saveState(String clientId, S state);
    void initializeState(String clientId);
}
