package com.yugam.ratelimiter.service;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.exception.ClientNotFoundException;
import com.yugam.ratelimiter.model.Client;
import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;
import com.yugam.ratelimiter.repository.ClientRepository;
import com.yugam.ratelimiter.strategy.RateLimiterStrategy;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class RateLimiterService {
    private final ClientRepository clientRepository;
    private final StrategyFactory strategyFactory;

    public RateLimiterService(ClientRepository clientRepository, StrategyFactory strategyFactory){
        this.clientRepository = clientRepository;
        this.strategyFactory = strategyFactory;
    }

    public RateLimitResponse handleRequest(String clientId){
        Client client = clientRepository.findByClientId(clientId).orElseThrow(() -> new ClientNotFoundException(clientId));

        RateLimitPolicy policy = client.getRateLimitPolicy();
        ClientRequestInfo requestInfo = client.getClientRequestInfo();

        RateLimiterStrategy strategy = strategyFactory.getStrategy(policy.getAlgorithm());
        return strategy.processRequest(requestInfo,policy,Instant.now());
    }
}
