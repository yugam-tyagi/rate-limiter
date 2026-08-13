package com.yugam.ratelimiter.model;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.policy.RateLimitPolicy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClientConfiguration {
    private final String clientId;
    private AlgorithmType algorithmType;
    private PolicyData policyData;
}
