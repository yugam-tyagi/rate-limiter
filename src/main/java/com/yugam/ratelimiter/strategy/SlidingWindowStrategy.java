package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;
import com.yugam.ratelimiter.model.SlidingWindowClientInfo;
import com.yugam.ratelimiter.model.SlidingWindowPolicy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Queue;

@Component
public class SlidingWindowStrategy implements RateLimiterStrategy<SlidingWindowClientInfo, SlidingWindowPolicy>{
    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.SLIDING_WINDOW;
    }

    @Override
    public RateLimitResponse processRequest(SlidingWindowClientInfo clientRequestInfo, SlidingWindowPolicy policy, Instant now) {
        synchronized (clientRequestInfo) {
            Queue<Instant> requestQueue = clientRequestInfo.getRequestTimestamps();
            long windowDuration = policy.getWindowDuration().toSeconds();
            int remainingRequests;
            long retryAfterSeconds;
            long elapsedTime;

            while(!requestQueue.isEmpty()){
                Instant firstTimestamp = requestQueue.peek();
                elapsedTime = Duration.between(firstTimestamp,now).toSeconds();
                if(elapsedTime >= windowDuration){
                    requestQueue.poll();
                }
                else{
                    break;
                }
            }

            if(requestQueue.size() >= policy.getMaxRequests()){
                elapsedTime = Duration.between(requestQueue.peek(),now).toSeconds();
                retryAfterSeconds = windowDuration-elapsedTime;
                throw new RateLimitExceededException(retryAfterSeconds);
            }

            requestQueue.add(now);
            remainingRequests = policy.getMaxRequests()-requestQueue.size();

            return new RateLimitResponse(true,remainingRequests);
        }
    }
}
