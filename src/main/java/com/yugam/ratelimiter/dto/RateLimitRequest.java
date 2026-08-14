package com.yugam.ratelimiter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RateLimitRequest {
    private final String clientId;
}
