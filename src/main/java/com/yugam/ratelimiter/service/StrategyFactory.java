package com.yugam.ratelimiter.service;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.StrategyNotFoundException;
import com.yugam.ratelimiter.strategy.RateLimiterStrategy;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;

@Component
public class StrategyFactory {
    private final EnumMap<AlgorithmType, RateLimiterStrategy> strategyMap = new EnumMap<>(AlgorithmType.class);

    public StrategyFactory(List<RateLimiterStrategy> strategies){
        for(RateLimiterStrategy strategy : strategies) {
            AlgorithmType algorithmType = strategy.getAlgorithmType();
            if(strategyMap.containsKey(algorithmType)) {
                throw new IllegalStateException("Duplicate Strategy found for: " + algorithmType);
            }
            strategyMap.put(algorithmType,strategy);
        }
    }

    public RateLimiterStrategy getStrategy(AlgorithmType algorithmType){
        RateLimiterStrategy strategy = strategyMap.get(algorithmType);

        if(strategy==null) {
            throw new StrategyNotFoundException(algorithmType);
        }

        return strategy;
    }
}
