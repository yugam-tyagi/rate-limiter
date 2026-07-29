package com.yugam.ratelimiter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Client {
    private final String clientId;
    private ClientRequestInfo clientRequestInfo;
    private RateLimitPolicy rateLimitPolicy;
}
