package com.yugam.ratelimiter.model;

import java.time.Instant;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ClientRequestInfo {
    private final String clientId;
    private int currentRequestCount;
    private Instant windowStartTime;

    public void incrementRequestCount() {
        currentRequestCount++;
    }

    public void startNewWindow(Instant now) {
        windowStartTime = now;
        currentRequestCount = 1;
    }
}
