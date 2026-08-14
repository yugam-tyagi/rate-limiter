package com.yugam.ratelimiter.service;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.repository.RedisRateLimitStateRepository;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;

@Component
public class StateRepositoryFactory {
    private final EnumMap<AlgorithmType, RedisRateLimitStateRepository> repositoryMap = new EnumMap<>(AlgorithmType.class);

    public StateRepositoryFactory(List<RedisRateLimitStateRepository> repositories){
        for(RedisRateLimitStateRepository repository : repositories) {
            AlgorithmType algorithmType = repository.getAlgorithmType();
            if(repositoryMap.containsKey(algorithmType)) {
                throw new IllegalStateException("Duplicate Repository found for: " + algorithmType);
            }
            repositoryMap.put(algorithmType,repository);
        }
    }

    public RedisRateLimitStateRepository getRepository(AlgorithmType algorithmType){
        RedisRateLimitStateRepository repository = repositoryMap.get(algorithmType);

//        if(repository==null) {
//            throw new RepositoryNotFoundException(algorithmType);
//        }

        return repository;
    }
}
