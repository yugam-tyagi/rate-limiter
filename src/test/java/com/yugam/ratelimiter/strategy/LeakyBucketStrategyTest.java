package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitRequest;
import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.clientState.LeakyBucketClientInfo;
import com.yugam.ratelimiter.model.policy.LeakyBucketPolicy;
import com.yugam.ratelimiter.model.state.LeakyBucketState;
import com.yugam.ratelimiter.repository.LeakyBucketStateRepository;
import com.yugam.ratelimiter.repository.RedisRateLimitStateRepository;
import com.yugam.ratelimiter.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class LeakyBucketStrategyTest {
    @Autowired
    private RateLimiterService service;
    @Autowired
    private LeakyBucketStateRepository repository;
    private RateLimiterStrategy strategy;

    @BeforeEach
    void setup(){
        strategy = new LeakyBucketStrategy();
    }

    @Test
    void shouldAllowRequestWhenBucketIsNotFull(){
        Instant now = Instant.now();
        LeakyBucketPolicy policy = new LeakyBucketPolicy(5,2);
        LeakyBucketState state = new LeakyBucketState(3,now);

        RateLimitResponse response = strategy.processRequest(state,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(1,response.getRemainingRequests());
        assertEquals(4,state.getCurrentRequestCount());
        assertEquals(now,state.getLastLeakTime());
    }

    @Test
    void shouldRejectRequestWhenBucketIsFull(){
        Instant now = Instant.now();
        LeakyBucketPolicy policy = new LeakyBucketPolicy(5,2);
        LeakyBucketState state = new LeakyBucketState(5,now);

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                ()->strategy.processRequest(state,policy,now)
        );

        assertEquals((long) Math.ceil(60.0/policy.getLeakRate()),exception.getRetryAfterSeconds());
        assertEquals(5,state.getCurrentRequestCount());
        assertEquals(now,state.getLastLeakTime());
    }

    @Test
    void shouldLeakRequestsBasedOnElapsedMinutes(){
        Instant now = Instant.now();
        LeakyBucketPolicy policy = new LeakyBucketPolicy(5,2);
        LeakyBucketState state = new LeakyBucketState(5,now.minusSeconds(70));

        RateLimitResponse response = strategy.processRequest(state,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(1,response.getRemainingRequests());
        assertEquals(4,state.getCurrentRequestCount());
        assertEquals(now.minusSeconds(10),state.getLastLeakTime());
    }

    @Test
    void shouldEmptyBucketAfterSufficientTime(){
        Instant now = Instant.now();
        LeakyBucketPolicy policy = new LeakyBucketPolicy(5,2);
        LeakyBucketState state = new LeakyBucketState(5,now.minusSeconds(320));

        RateLimitResponse response = strategy.processRequest(state,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(4,response.getRemainingRequests());
        assertEquals(1,state.getCurrentRequestCount());
        assertEquals(now.minusSeconds(20),state.getLastLeakTime());
    }

    @Test
    void shouldAllowOnlyBucketCapacityRequestsWhenMultipleThreadsAccessSameClient(){
        String clientId = "Mohit";
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
                catch (RateLimitExceededException exception){
                    failureCount.incrementAndGet();
                } catch (InterruptedException exception) {
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
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        finally {
            executor.shutdown();
        }
    }
}
