package com.yugam.ratelimiter.model;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ClientRequestInfo {
    private final String clientId;
    private int currentRequestCount;
    private Instant windowStartTime;

    private final Queue<Instant> requestTimestamps = new ArrayDeque<>();

    private int availableTokens;
    private Instant lastRefillTime;

    public void incrementRequestCount() {
        currentRequestCount++;
    }

    public void startNewWindow(Instant now) {
        windowStartTime = now;
        currentRequestCount = 1;
    }

    public void refreshTokensAndRefillTime(int tokens, Instant instant){
        availableTokens = tokens;
        lastRefillTime = instant;
    }
}
