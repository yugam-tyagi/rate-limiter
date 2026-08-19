package com.yugam.ratelimiter.exception.exceptions;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException{
    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds){
        super("Please retry in "+retryAfterSeconds+" seconds.");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
