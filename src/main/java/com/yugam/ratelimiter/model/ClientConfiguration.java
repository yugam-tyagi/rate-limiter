package com.yugam.ratelimiter.model;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.policy.RateLimitPolicy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClientConfiguration {
    @NotBlank(message = "ClientId can't be empty.")
    private final String clientId;
    @NotNull(message = "Algorithm type can't be null.")
    private AlgorithmType algorithmType;
    @Valid
    @NotNull(message = "Policy data can't be null.")
    private PolicyData policyData;
}
