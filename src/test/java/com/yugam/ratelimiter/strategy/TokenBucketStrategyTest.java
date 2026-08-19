package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitRequest;
import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.exception.exceptions.RateLimitExceededException;
import com.yugam.ratelimiter.model.policy.TokenBucketPolicy;
import com.yugam.ratelimiter.model.state.TokenBucketState;
import com.yugam.ratelimiter.repository.clientStateRepository.TokenBucketStateRepository;
import com.yugam.ratelimiter.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TokenBucketStrategyTest {
    @Autowired
    private RateLimiterService service;
    @Autowired
    private TokenBucketStateRepository repository;
    private RateLimiterStrategy strategy;

    @BeforeEach
    void setup(){
        strategy = new TokenBucketStrategy();
    }

    @Test
    void shouldAllowRequestWhenTokensAreAvailable(){
        TokenBucketPolicy policy = new TokenBucketPolicy(10,2);
        Instant now = Instant.now();
        TokenBucketState state = new TokenBucketState(1,now.minusSeconds(10));

        RateLimitResponse response = strategy.processRequest(state,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(0,state.getAvailableTokens());
        assertEquals(0,response.getRemainingRequests());
        assertEquals(now.minusSeconds(10),state.getLastRefillTime());
    }

    @Test
    void shouldRejectRequestWhenNoTokensAreAvailable(){
        TokenBucketPolicy policy = new TokenBucketPolicy(10,2);
        Instant now = Instant.now();
        TokenBucketState state = new TokenBucketState(0,now.minusSeconds(10));

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> strategy.processRequest(state, policy, now)
        );

        assertEquals((long) Math.ceil(60.0/policy.getRefillRate()),exception.getRetryAfterSeconds());
        assertEquals(0,state.getAvailableTokens());
        assertEquals(now.minusSeconds(10),state.getLastRefillTime());
    }

    @Test
    void shouldAllowRequestAfterPartialTokenRefill(){
        TokenBucketPolicy policy = new TokenBucketPolicy(10,2);
        Instant now = Instant.now();
        TokenBucketState state = new TokenBucketState(0,now.minusSeconds(120));

        RateLimitResponse response = strategy.processRequest(state,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(3,state.getAvailableTokens());
        assertEquals(3,response.getRemainingRequests());
        assertEquals(now,state.getLastRefillTime());
    }

    @Test
    void shouldNotExceedBucketCapacityWhenRefilling(){
        TokenBucketPolicy policy = new TokenBucketPolicy(10,6);
        Instant now = Instant.now();
        TokenBucketState state = new TokenBucketState(0,now.minusSeconds(120));

        RateLimitResponse response = strategy.processRequest(state,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(9,state.getAvailableTokens());
        assertEquals(9,response.getRemainingRequests());
        assertEquals(now,state.getLastRefillTime());
    }

    @Test
    void shouldAllowOnlyBucketCapacityRequestsWhenMultipleThreadsAccessSameClient(){
        String clientId = "Pawan";
        RateLimitRequest request = new RateLimitRequest(clientId);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finsihLatch = new CountDownLatch(10);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for(int i=0;i<10;i++){
            executor.submit(()->{
                try{
                    startLatch.await();
                    service.handleRequest(request);
                    successCount.incrementAndGet();
                }
                catch (RateLimitExceededException ex){
                    failureCount.incrementAndGet();
                }
                catch (InterruptedException ex){
                    Thread.currentThread().interrupt();
                }
                finally {
                    finsihLatch.countDown();
                }
            });
        }

        try{
            startLatch.countDown();
            finsihLatch.await();
            assertEquals(4,successCount.get());
            assertEquals(6,failureCount.get());
            assertEquals(0,repository.getState(clientId).getAvailableTokens());
            assertEquals(Instant.now().truncatedTo(ChronoUnit.MINUTES),repository.getState(clientId).getLastRefillTime().truncatedTo(ChronoUnit.MINUTES));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        finally {
            executor.shutdown();
        }
    }
}
