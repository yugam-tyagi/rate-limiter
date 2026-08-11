package com.yugam.ratelimiter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.Duration;

@Getter
@Builder
@AllArgsConstructor
public class FixedWindowPolicy implements RateLimitPolicy{
    private final int maxRequests;
    private final Duration windowDuration;
}
