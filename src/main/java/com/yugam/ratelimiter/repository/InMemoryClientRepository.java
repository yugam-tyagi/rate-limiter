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
        RateLimitPolicy defaultPolicy1 = new RateLimitPolicy(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_DURATION, AlgorithmType.FIXED_WINDOW,0);
        RateLimitPolicy defaultPolicy2 = new RateLimitPolicy(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_DURATION, AlgorithmType.SLIDING_WINDOW,0);
        RateLimitPolicy defaultPolicy3 = new RateLimitPolicy(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_DURATION, AlgorithmType.TOKEN_BUCKET,3);

        addClient("client1",defaultPolicy1);
        addClient("client2",defaultPolicy2);
        addClient("client3",defaultPolicy3);

    }

    private void addClient(String clientId, RateLimitPolicy policy){
        ClientRequestInfo clientRequestInfo = new ClientRequestInfo(clientId,0,Instant.now(),0,Instant.now().minusSeconds(2));
        Client client = new Client(clientId,clientRequestInfo,policy);
        clients.put(clientId,client);
    }
}
