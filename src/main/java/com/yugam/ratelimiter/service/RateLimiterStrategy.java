package com.yugam.ratelimiter.service;

import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;

import java.time.Instant;

public interface RateLimiterStrategy {

    boolean processRequest(ClientRequestInfo clientRequestInfo, RateLimitPolicy policy, Instant instant);
}
