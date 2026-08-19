package com.yugam.ratelimiter.exception.exceptions;

public class InvalidPolicyException extends RuntimeException {

    public InvalidPolicyException(String message) {
        super(message);
    }
}