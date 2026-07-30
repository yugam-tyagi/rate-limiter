package com.yugam.ratelimiter.dto;

import lombok.Getter;

@Getter
public class RateLimitResponse {
    private final boolean allowed;
    private final int remainingRequests;
    private final long windowResetsInSeconds;

    public RateLimitResponse(boolean allowed, int remainingRequests, long windowResetsInSeconds){
        this.allowed = allowed;
        this.remainingRequests = remainingRequests;
        this.windowResetsInSeconds = windowResetsInSeconds;
    }
}
