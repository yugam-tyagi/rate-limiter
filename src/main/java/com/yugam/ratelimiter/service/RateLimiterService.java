package com.yugam.ratelimiter.service;

import com.yugam.ratelimiter.dto.RateLimitRequest;
import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.ClientNotFoundException;
import com.yugam.ratelimiter.model.ClientConfiguration;
import com.yugam.ratelimiter.model.PolicyData;
import com.yugam.ratelimiter.model.policy.RateLimitPolicy;
import com.yugam.ratelimiter.model.state.RateLimitState;
import com.yugam.ratelimiter.repository.RedisClientConfigurationRepository;
import com.yugam.ratelimiter.repository.RedisRateLimitStateRepository;
import com.yugam.ratelimiter.strategy.RateLimiterStrategy;
import com.yugam.ratelimiter.validator.PolicyValidator;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Slf4j
@Service
public class RateLimiterService {
    private final StrategyFactory strategyFactory;
    private final PolicyFactory policyFactory;
    private final StateRepositoryFactory repositoryFactory;
    private final RedisLockService redisLockService;
    private final PolicyValidator policyValidator;
    private final RedisClientConfigurationRepository redisClientConfigurationRepository;

    public RateLimiterService(StrategyFactory strategyFactory, PolicyFactory policyFactory, StateRepositoryFactory repositoryFactory, RedisLockService redisLockService, PolicyValidator policyValidator, RedisClientConfigurationRepository redisClientConfigurationRepository){
        this.strategyFactory = strategyFactory;
        this.policyFactory = policyFactory;
        this.repositoryFactory = repositoryFactory;
        this.redisLockService = redisLockService;
        this.policyValidator = policyValidator;
        this.redisClientConfigurationRepository = redisClientConfigurationRepository;
    }

    public void createClient(ClientConfiguration configuration){
        String clientId = configuration.getClientId();
        AlgorithmType algorithmType = configuration.getAlgorithmType();
        PolicyData data = configuration.getPolicyData();

        policyValidator.validate(algorithmType,data);

        redisClientConfigurationRepository.save(clientId,algorithmType,data);
        RedisRateLimitStateRepository stateRepository = repositoryFactory.getRepository(algorithmType);
        stateRepository.initializeState(clientId);
    }

    public ClientConfiguration getClient(String clientId){
        return redisClientConfigurationRepository.get(clientId);
    }

    public void resetState(RateLimitRequest request){
        String clientId = request.getClientId();
        ClientConfiguration clientConfiguration = redisClientConfigurationRepository.get(clientId);
        AlgorithmType algorithmType = clientConfiguration.getAlgorithmType();
        RedisRateLimitStateRepository repository = repositoryFactory.getRepository(algorithmType);

        repository.initializeState(clientId);
    }

    public RateLimitResponse handleRequest(RateLimitRequest request){
        String clientId = request.getClientId();
        ClientConfiguration clientConfiguration = redisClientConfigurationRepository.get(clientId);
        AlgorithmType algorithmType = clientConfiguration.getAlgorithmType();
        PolicyData policyData = clientConfiguration.getPolicyData();

        RateLimitPolicy policy = policyFactory.getPolicy(algorithmType,policyData);
        RateLimiterStrategy strategy = strategyFactory.getStrategy(algorithmType);
        RedisRateLimitStateRepository repository = repositoryFactory.getRepository(algorithmType);

        RLock lock = redisLockService.getLock(clientId);

        try{
            lock.lock();
            RateLimitState state = (RateLimitState) repository.getState(clientId);
            log.info("Client: {}, State: {}", clientId, state);
            RateLimitResponse response = strategy.processRequest(state,policy,Instant.now());
            repository.saveState(clientId,state);
            return response;
        }
        finally {
            if(lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
