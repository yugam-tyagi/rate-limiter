package com.yugam.ratelimiter.dto;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.PolicyData;
import com.yugam.ratelimiter.model.policy.RateLimitPolicy;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClientConfigurationRequest {
    private final String clientId;
    private AlgorithmType algorithmType;
    private PolicyData policyData;
}
