package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.policy.FixedWindowPolicy;
import com.yugam.ratelimiter.model.state.FixedWindowState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class FixedWindowStrategy implements RateLimiterStrategy<FixedWindowState, FixedWindowPolicy> {

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.FIXED_WINDOW;
    }

    @Override
    public RateLimitResponse processRequest(FixedWindowState clientState, FixedWindowPolicy policy, Instant now) {
        Instant windowStart = clientState.getWindowStartTime();
        long elapsedTimeInSeconds = windowStart==null ? 0 : Duration.between(windowStart,now).toSeconds();
        long windowDurationInSeconds = policy.getWindowDuration().toSeconds();
        long windowResetsInSeconds;

        if (windowStart==null || elapsedTimeInSeconds >= windowDurationInSeconds) {
            clientState.setWindowStartTime(now);
            clientState.setCurrentRequestCount(1);
        } else if (clientState.getCurrentRequestCount() < policy.getMaxRequests()) {
            clientState.setCurrentRequestCount(clientState.getCurrentRequestCount()+1);
        } else {
            windowResetsInSeconds = windowDurationInSeconds - elapsedTimeInSeconds;
            throw new RateLimitExceededException(windowResetsInSeconds);
        }

        int remainingRequests = Math.max(0, policy.getMaxRequests() - clientState.getCurrentRequestCount());

        log.info("Request allowed. Remaining requests: {}",
                remainingRequests);
        return new RateLimitResponse(true, remainingRequests);
    }
}
