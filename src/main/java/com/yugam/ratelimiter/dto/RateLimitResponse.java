package com.yugam.ratelimiter.dto;

import lombok.Getter;

@Getter
public class RateLimitResponse {
    private final boolean allowed;
    private final int remainingRequests;

    public RateLimitResponse(boolean allowed, int remainingRequests){
        this.allowed = allowed;
        this.remainingRequests = remainingRequests;
    }
}
