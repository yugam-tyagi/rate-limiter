package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;
import com.yugam.ratelimiter.model.SlidingWindowClientInfo;
import com.yugam.ratelimiter.model.SlidingWindowPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SlidingWindowStrategyTest {
    private RateLimiterStrategy strategy;

    @BeforeEach
    void setup(){
        strategy = new SlidingWindowStrategy();
    }

    @Test
    void shouldAllowRequestWhenQueueSizeIsBelowLimit(){
        SlidingWindowPolicy policy = new SlidingWindowPolicy(3,Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-08-06T10:00:00Z");
        SlidingWindowClientInfo clientRequestInfo = new SlidingWindowClientInfo("2");
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(40));
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(30));

        RateLimitResponse response = strategy.processRequest(clientRequestInfo,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(0,response.getRemainingRequests());
        assertEquals(3,clientRequestInfo.getRequestTimestamps().size());
    }

    @Test
    void shouldRejectRequestWhenQueueSizeEqualsLimit(){
        SlidingWindowPolicy policy = new SlidingWindowPolicy(3,Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-08-06T10:00:00Z");
        SlidingWindowClientInfo clientRequestInfo = new SlidingWindowClientInfo("2");
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(40));
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(30));
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(20));

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> strategy.processRequest(clientRequestInfo, policy, now)
        );

        assertEquals(3,clientRequestInfo.getRequestTimestamps().size());
        assertEquals(20,exception.getRetryAfterSeconds());
    }

    @Test
    void shouldRemoveExpiredRequestsBeforeAllowing(){
        SlidingWindowPolicy policy = new SlidingWindowPolicy(3,Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-08-06T10:00:00Z");
        SlidingWindowClientInfo clientRequestInfo = new SlidingWindowClientInfo("2");
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(80));
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(70));
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(55));

        RateLimitResponse response = strategy.processRequest(clientRequestInfo,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(1,response.getRemainingRequests());
        assertEquals(2,clientRequestInfo.getRequestTimestamps().size());
    }

    @Test
    void shouldAllowOnlyMaxRequestsWhenMultipleThreadsAccessSameClient(){
        SlidingWindowPolicy policy = new SlidingWindowPolicy(5,Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-08-06T10:00:00Z");
        SlidingWindowClientInfo clientRequestInfo = new SlidingWindowClientInfo("2");
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(80));
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(70));
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(55));
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(45));
        clientRequestInfo.getRequestTimestamps().offer(now.minusSeconds(35));

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(10);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for(int i=0;i<10;i++){
            executor.submit(()->{
                try{
                    startLatch.await();
                    strategy.processRequest(clientRequestInfo,policy,now);
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
            assertEquals(2,successCount.get());
            assertEquals(8,failureCount.get());
            assertEquals(5,clientRequestInfo.getRequestTimestamps().size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        finally {
            executor.shutdown();
        }
    }
}
