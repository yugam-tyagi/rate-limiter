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
public class TokenBucketState implements RateLimitState{
    private int availableTokens;
    private Instant lastRefillTime;
}
