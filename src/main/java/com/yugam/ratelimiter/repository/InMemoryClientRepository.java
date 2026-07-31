package com.yugam.ratelimiter.repository;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.Client;
import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;
import jakarta.annotation.*;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryClientRepository implements ClientRepository{
    private static final int DEFAULT_MAX_REQUESTS = 3;
    private static final Duration DEFAULT_WINDOW_DURATION = Duration.ofMinutes(1);
    private final Map<String, Client> clients= new HashMap<>();

    @Override
    public Optional<Client> findByClientId(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    @PostConstruct
    public void init() {
        RateLimitPolicy defaultPolicy = new RateLimitPolicy(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_DURATION, AlgorithmType.FIXED_WINDOW);

        addClient("client1",defaultPolicy);
        addClient("client2",defaultPolicy);
    }

    private void addClient(String clientId, RateLimitPolicy policy){
        ClientRequestInfo clientRequestInfo = new ClientRequestInfo(clientId,0,Instant.now());
        Client client = new Client(clientId,clientRequestInfo,policy);
        clients.put(clientId,client);
    }
}
