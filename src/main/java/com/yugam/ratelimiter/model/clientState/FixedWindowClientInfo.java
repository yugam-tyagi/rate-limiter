package com.yugam.ratelimiter.model.clientState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class FixedWindowClientInfo implements ClientRequestInfo{
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

    @Override
    public String getClientId() {
        return clientId;
    }
}
