package com.yugam.ratelimiter.model.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LeakyBucketState implements RateLimitState{
    private int currentRequestCount;
    private Instant lastLeakTime;
}
