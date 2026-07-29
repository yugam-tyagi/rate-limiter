package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class FixedWindowStrategy implements RateLimiterStrategy {

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.FIXED_WINDOW;
    }

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
