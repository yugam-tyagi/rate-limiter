package com.yugam.ratelimiter.exception;

public class InvalidPolicyException extends RuntimeException {

    public InvalidPolicyException(String message) {
        super(message);
    }
}