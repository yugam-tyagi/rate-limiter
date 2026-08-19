package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.exceptions.RateLimitExceededException;
import com.yugam.ratelimiter.model.policy.SlidingWindowPolicy;
import com.yugam.ratelimiter.model.state.SlidingWindowState;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;

@Component
public class SlidingWindowStrategy implements RateLimiterStrategy<SlidingWindowState, SlidingWindowPolicy>{
    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.SLIDING_WINDOW;
    }

    @Override
    public RateLimitResponse processRequest(SlidingWindowState clientState, SlidingWindowPolicy policy, Instant now) {
        Deque<Instant> timestamps = clientState.getTimestamps();
        long windowDuration = policy.getWindowDuration().toSeconds();
        int remainingRequests;
        long retryAfterSeconds;
        long elapsedTime;

        while(!timestamps.isEmpty()){
            Instant firstTimestamp = timestamps.peekFirst();
            elapsedTime = Duration.between(firstTimestamp,now).toSeconds();
            if(elapsedTime >= windowDuration){
                timestamps.removeFirst();
            }
            else{
                break;
            }
        }

        if(timestamps.size() >= policy.getMaxRequests()){
            elapsedTime = Duration.between(timestamps.peekFirst(),now).toSeconds();
            retryAfterSeconds = windowDuration-elapsedTime;
            throw new RateLimitExceededException(retryAfterSeconds);
        }

        timestamps.addLast(now);
        remainingRequests = policy.getMaxRequests()-timestamps.size();

        return new RateLimitResponse(true,remainingRequests);
    }
}
