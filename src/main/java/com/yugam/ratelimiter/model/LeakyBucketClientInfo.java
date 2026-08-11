package com.yugam.ratelimiter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class LeakyBucketClientInfo implements ClientRequestInfo{
    private final String clientId;
    private int currentRequestCount;
    private Instant lastLeakTime;

    public void refreshRequestCountAndLeakTime(int requestCount, Instant leakTime){
        currentRequestCount = requestCount;
        lastLeakTime = leakTime;
    }

    @Override
    public String getClientId() {
        return clientId;
    }
}
