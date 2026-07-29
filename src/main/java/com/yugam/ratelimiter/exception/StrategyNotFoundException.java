package com.yugam.ratelimiter.exception;

import com.yugam.ratelimiter.enums.AlgorithmType;

public class StrategyNotFoundException extends RuntimeException{
    public StrategyNotFoundException(AlgorithmType algorithmType){
        super("Strategy not found for: "+algorithmType);
    }
}
