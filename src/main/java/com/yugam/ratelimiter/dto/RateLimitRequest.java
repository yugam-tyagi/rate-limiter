package com.yugam.ratelimiter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RateLimitRequest {
    @NotBlank(message = "ClientId can't be empty.")
    private final String clientId;
}
