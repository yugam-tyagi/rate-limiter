package com.yugam.ratelimiter.model.policy;

import com.yugam.ratelimiter.enums.AlgorithmType;
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

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.SLIDING_WINDOW;
    }
}
