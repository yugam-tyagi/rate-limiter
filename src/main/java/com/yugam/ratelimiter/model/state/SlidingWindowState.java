package com.yugam.ratelimiter.model.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.Deque;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SlidingWindowState implements RateLimitState{
    private Deque<Instant> timestamps;
}
