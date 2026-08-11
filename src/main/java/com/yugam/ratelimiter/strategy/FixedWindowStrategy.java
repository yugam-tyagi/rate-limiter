package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.FixedWindowClientInfo;
import com.yugam.ratelimiter.model.FixedWindowPolicy;
import com.yugam.ratelimiter.model.RateLimitPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class FixedWindowStrategy implements RateLimiterStrategy<FixedWindowClientInfo, FixedWindowPolicy> {

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.FIXED_WINDOW;
    }

    @Override
    public RateLimitResponse processRequest(FixedWindowClientInfo clientRequestInfo, FixedWindowPolicy policy, Instant now) {
        synchronized (clientRequestInfo) {
            Instant windowStart = clientRequestInfo.getWindowStartTime();
            Duration elapsedDuration = Duration.between(windowStart, now);
            long elapsedTimeInSeconds = elapsedDuration.toSeconds();
            long windowDurationInSeconds = policy.getWindowDuration().toSeconds();
            long windowResetsInSeconds;

            if (elapsedTimeInSeconds >= windowDurationInSeconds) {
                clientRequestInfo.startNewWindow(now);
            } else if (clientRequestInfo.getCurrentRequestCount() < policy.getMaxRequests()) {
                clientRequestInfo.incrementRequestCount();
            } else {
                windowResetsInSeconds = windowDurationInSeconds - elapsedTimeInSeconds;
                throw new RateLimitExceededException(windowResetsInSeconds);
            }

            int remainingRequests = Math.max(0, policy.getMaxRequests() - clientRequestInfo.getCurrentRequestCount());

            log.info("Request allowed for clientId: {}. Requests remaining: {}",
                    clientRequestInfo.getClientId(),
                    remainingRequests);
            return new RateLimitResponse(true, remainingRequests);
        }
    }
}
