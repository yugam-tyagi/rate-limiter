package com.yugam.ratelimiter.model;

import com.yugam.ratelimiter.enums.AlgorithmType;
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
    private AlgorithmType algorithmType;
}
