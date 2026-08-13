package com.yugam.ratelimiter.repository;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.*;
import com.yugam.ratelimiter.model.clientState.*;
import com.yugam.ratelimiter.model.policy.*;
import jakarta.annotation.*;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryClientRepository implements ClientRepository{
    private static final int DEFAULT_MAX_REQUESTS = 5;
    private static final Duration DEFAULT_WINDOW_DURATION = Duration.ofMinutes(1);
    private final Map<String, Client> clients= new HashMap<>();

    @Override
    public Optional<Client> findByClientId(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    @PostConstruct
    public void init() {
        FixedWindowClientInfo fixedWindowClient = new FixedWindowClientInfo("client1",0,Instant.now());
        SlidingWindowClientInfo slidingWindowClient = new SlidingWindowClientInfo("client2");
        TokenBucketClientInfo tokenBucketClient = new TokenBucketClientInfo("client3",0,Instant.now().minusSeconds(120));
        LeakyBucketClientInfo leakyBucketClient = new LeakyBucketClientInfo("client4",0,Instant.now().minusSeconds(120));

        FixedWindowPolicy fixedWindowPolicy = new FixedWindowPolicy(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_DURATION);
        SlidingWindowPolicy slidingWindowPolicy = new SlidingWindowPolicy(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_DURATION);
        TokenBucketPolicy tokenBucketPolicy = new TokenBucketPolicy(DEFAULT_MAX_REQUESTS,3);
        LeakyBucketPolicy leakyBucketPolicy = new LeakyBucketPolicy(DEFAULT_MAX_REQUESTS,2);

        addClient(fixedWindowClient,fixedWindowPolicy,AlgorithmType.FIXED_WINDOW);
        addClient(slidingWindowClient,slidingWindowPolicy,AlgorithmType.SLIDING_WINDOW);
        addClient(tokenBucketClient,tokenBucketPolicy,AlgorithmType.TOKEN_BUCKET);
        addClient(leakyBucketClient,leakyBucketPolicy,AlgorithmType.LEAKY_BUCKET);
    }

    private void addClient(ClientRequestInfo info, RateLimitPolicy policy, AlgorithmType algorithm){
        Client client = new Client(info.getClientId(),info,policy,algorithm);
        clients.put(info.getClientId(),client);
    }
}
