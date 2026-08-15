package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitRequest;
import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.ClientConfiguration;
import com.yugam.ratelimiter.model.clientState.SlidingWindowClientInfo;
import com.yugam.ratelimiter.model.policy.SlidingWindowPolicy;
import com.yugam.ratelimiter.model.state.SlidingWindowState;
import com.yugam.ratelimiter.repository.RedisRateLimitStateRepository;
import com.yugam.ratelimiter.repository.SlidingWindowStateRepository;
import com.yugam.ratelimiter.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SlidingWindowStrategyTest {
    @Autowired
    private RateLimiterService service;
    @Autowired
    private SlidingWindowStateRepository repository;
    private RateLimiterStrategy strategy;

    @BeforeEach
    void setup(){
        strategy = new SlidingWindowStrategy();
    }

    @Test
    void shouldAllowRequestWhenQueueSizeIsBelowLimit(){
        SlidingWindowPolicy policy = new SlidingWindowPolicy(3,Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-08-06T10:00:00Z");
        SlidingWindowState state = new SlidingWindowState(new ArrayDeque<>());
        state.getTimestamps().addLast(now.minusSeconds(40));
        state.getTimestamps().addLast(now.minusSeconds(30));

        RateLimitResponse response = strategy.processRequest(state,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(0,response.getRemainingRequests());
        assertEquals(3,state.getTimestamps().size());
    }

    @Test
    void shouldRejectRequestWhenQueueSizeEqualsLimit(){
        SlidingWindowPolicy policy = new SlidingWindowPolicy(3,Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-08-06T10:00:00Z");
        SlidingWindowState state = new SlidingWindowState(new ArrayDeque<>());
        state.getTimestamps().addLast(now.minusSeconds(40));
        state.getTimestamps().addLast(now.minusSeconds(30));
        state.getTimestamps().addLast(now.minusSeconds(20));

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> strategy.processRequest(state, policy, now)
        );

        assertEquals(3,state.getTimestamps().size());
        assertEquals(20,exception.getRetryAfterSeconds());
    }

    @Test
    void shouldRemoveExpiredRequestsBeforeAllowing(){
        SlidingWindowPolicy policy = new SlidingWindowPolicy(3,Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-08-06T10:00:00Z");
        SlidingWindowState state = new SlidingWindowState(new ArrayDeque<>());
        state.getTimestamps().addLast(now.minusSeconds(80));
        state.getTimestamps().addLast(now.minusSeconds(70));
        state.getTimestamps().addLast(now.minusSeconds(60));

        RateLimitResponse response = strategy.processRequest(state,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(2,response.getRemainingRequests());
        assertEquals(1,state.getTimestamps().size());
    }

    @Test
    void shouldAllowOnlyMaxRequestsWhenMultipleThreadsAccessSameClient(){
        String clientId="Rahul";
        RateLimitRequest request = new RateLimitRequest(clientId);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(10);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for(int i=0;i<10;i++){
            executor.submit(()->{
                try{
                    startLatch.await();
                    service.handleRequest(request);
                    successCount.incrementAndGet();
                }
                catch(RateLimitExceededException ex){
                    failureCount.incrementAndGet();
                }
                catch(InterruptedException ex){
                    Thread.currentThread().interrupt();
                }
                finally {
                    finishLatch.countDown();
                }
            });
        }

        try{
            startLatch.countDown();
            finishLatch.await();
            assertEquals(4,successCount.get());
            assertEquals(6,failureCount.get());
            assertEquals(4,repository.getState(clientId).getTimestamps().size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        finally {
            executor.shutdown();
        }
    }
}
