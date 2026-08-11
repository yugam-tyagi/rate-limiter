package com.yugam.ratelimiter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class TokenBucketClientInfo implements ClientRequestInfo{
    private final String clientId;
    private int availableTokens;
    private Instant lastRefillTime;

    public void refreshTokensAndRefillTime(int tokens, Instant instant){
        availableTokens = tokens;
        lastRefillTime = instant;
    }

    @Override
    public String getClientId() {
        return clientId;
    }
}
