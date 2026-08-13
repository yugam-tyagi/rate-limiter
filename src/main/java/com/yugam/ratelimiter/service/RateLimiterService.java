package com.yugam.ratelimiter.service;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.ClientNotFoundException;
import com.yugam.ratelimiter.model.Client;
import com.yugam.ratelimiter.model.ClientConfiguration;
import com.yugam.ratelimiter.model.PolicyData;
import com.yugam.ratelimiter.model.clientState.ClientRequestInfo;
import com.yugam.ratelimiter.model.policy.RateLimitPolicy;
import com.yugam.ratelimiter.repository.ClientRepository;
import com.yugam.ratelimiter.repository.RedisClientConfigurationRepository;
import com.yugam.ratelimiter.strategy.RateLimiterStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Slf4j
@Service
public class RateLimiterService {
    private final ClientRepository clientRepository;
    private final StrategyFactory strategyFactory;
    private final PolicyFactory policyFactory;
    private final RedisClientConfigurationRepository redisClientConfigurationRepository;

    public RateLimiterService(ClientRepository clientRepository, StrategyFactory strategyFactory, PolicyFactory policyFactory, RedisClientConfigurationRepository redisClientConfigurationRepository){
        this.clientRepository = clientRepository;
        this.strategyFactory = strategyFactory;
        this.policyFactory = policyFactory;
        this.redisClientConfigurationRepository = redisClientConfigurationRepository;
    }

    public void createClient(ClientConfiguration configuration){
        redisClientConfigurationRepository.save(
                configuration.getClientId(),
                configuration.getAlgorithmType(),
                configuration.getPolicyData());
    }

    public ClientConfiguration getClient(String clientId){
        return redisClientConfigurationRepository.get(clientId);
    }

    public RateLimitResponse handleRequest(String clientId){
        log.info("Fetching client and policy for clientId: {}",clientId);
        Client client = clientRepository.findByClientId(clientId).orElseThrow(() -> new ClientNotFoundException(clientId));

        RateLimitPolicy policy = client.getRateLimitPolicy();
        ClientRequestInfo requestInfo = client.getClientRequestInfo();

        RateLimiterStrategy strategy = strategyFactory.getStrategy(client.getAlgorithmType());
        log.info("Processing rate limit request for clientId: {} using algorithm: {}",clientId,client.getAlgorithmType());
        return strategy.processRequest(requestInfo,policy,Instant.now());
    }
}
