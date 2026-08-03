package com.yugam.ratelimiter.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException{
    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds){
        super("Please retry in "+retryAfterSeconds+" seconds.");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
