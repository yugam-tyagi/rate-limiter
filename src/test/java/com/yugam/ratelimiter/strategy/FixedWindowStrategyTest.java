package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitRequest;
import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.policy.FixedWindowPolicy;
import com.yugam.ratelimiter.model.state.FixedWindowState;
import com.yugam.ratelimiter.repository.RedisRateLimitStateRepository;
import com.yugam.ratelimiter.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class FixedWindowStrategyTest {
    @Autowired
    private RateLimiterService service;
    @Autowired
    private RedisRateLimitStateRepository repository;
    private FixedWindowStrategy strategy;

    @BeforeEach
    void setup(){
        strategy = new FixedWindowStrategy();
    }

    @Test
    void shouldStartNewWindowWhenWindowStartIsNull(){
        FixedWindowPolicy policy = new FixedWindowPolicy(3, Duration.ofMinutes(1));
        Instant now = Instant.now();
        FixedWindowState state = new FixedWindowState(0,null);

        RateLimitResponse response = strategy.processRequest(state,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(1,state.getCurrentRequestCount());
        assertEquals(now,state.getWindowStartTime());
    }

    @Test
    void shouldAllowRequestWhenRequestCountIsBelowLimit(){
        FixedWindowPolicy policy = new FixedWindowPolicy(3, Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        Instant originalWindowStart = now.minusSeconds(20);
        FixedWindowState state = new FixedWindowState(2,originalWindowStart);

        RateLimitResponse response = strategy.processRequest(state,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(3,state.getCurrentRequestCount());
        assertEquals(originalWindowStart,state.getWindowStartTime());
    }

    @Test
    void shouldRejectRequestWhenRequestCountEqualsLimit(){
        FixedWindowPolicy policy = new FixedWindowPolicy(3, Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        Instant originalWindowStart = now.minusSeconds(20);
        FixedWindowState state = new FixedWindowState(3,originalWindowStart);

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> strategy.processRequest(state, policy, now)
        );
        assertEquals(3,state.getCurrentRequestCount());
        assertEquals(originalWindowStart,state.getWindowStartTime());
        assertEquals(40,exception.getRetryAfterSeconds());
    }

    @Test
    void shouldStartNewWindowWhenWindowExpires(){
        FixedWindowPolicy policy = new FixedWindowPolicy(3, Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        Instant originalWindowStart = now.minusSeconds(60);
        FixedWindowState state = new FixedWindowState(3,originalWindowStart);

        RateLimitResponse response = strategy.processRequest(state,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(1,state.getCurrentRequestCount());
        assertEquals(now,state.getWindowStartTime());
    }

    @Test
    void shouldAllowOnlyMaxRequestsWhenMultipleThreadsAccessSameClient() {
        String clientId = "Yugam";
        repository.initializeState(clientId);
        RateLimitRequest request = new RateLimitRequest(clientId);
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(20);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for(int i=0;i<20;i++){
            executor.submit(()->{
                try {
                    startLatch.await();
                    service.handleRequest(request);
                    successCount.incrementAndGet();
                }catch (RateLimitExceededException ex){
                    failureCount.incrementAndGet();
                } catch (InterruptedException e) {
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
            assertEquals(3,successCount.get());
            assertEquals(17,failureCount.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }
}