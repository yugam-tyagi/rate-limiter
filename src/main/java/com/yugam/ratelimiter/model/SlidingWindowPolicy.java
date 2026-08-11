package com.yugam.ratelimiter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.Duration;

@Getter
@Builder
@AllArgsConstructor
public class SlidingWindowPolicy implements RateLimitPolicy{
    private final int maxRequests;
    private final Duration windowDuration;
}
