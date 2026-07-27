package com.yugam.ratelimiter.service;

import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;

import java.time.Duration;
import java.time.Instant;

public class FixedWindowStrategy implements RateLimiterStrategy{

    @Override
    public boolean processRequest(ClientRequestInfo clientRequestInfo, RateLimitPolicy policy, Instant now) {
        Instant start = clientRequestInfo.getWindowStartTime();
        Duration elapsedDuration = Duration.between(start,now);
        long elapsedTime = elapsedDuration.toSeconds();
        long windowDuration = policy.getWindowDuration().toSeconds();

        if(elapsedTime>=windowDuration){
            clientRequestInfo.startNewWindow(now);
            return true;
        }


        if(clientRequestInfo.getCurrentRequestCount()<policy.getMaxRequests()){
            clientRequestInfo.incrementRequestCount();
            return true;
        }

        return false;
    }
}
