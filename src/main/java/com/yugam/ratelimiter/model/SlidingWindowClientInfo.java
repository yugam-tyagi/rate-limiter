package com.yugam.ratelimiter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;

@Getter
@Builder
@AllArgsConstructor
public class SlidingWindowClientInfo implements ClientRequestInfo{
    private final String clientId;
    private final Queue<Instant> requestTimestamps = new ArrayDeque<>();

    @Override
    public String getClientId() {
        return clientId;
    }
}
