package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedWindowStrategyTest {
    private RateLimiterStrategy strategy;

    @BeforeEach
    void setup(){
        strategy = new FixedWindowStrategy();
    }

    @Test
    void shouldAllowRequestWhenRequestCountIsBelowLimit(){
        RateLimitPolicy policy = new RateLimitPolicy(3, Duration.ofMinutes(1), AlgorithmType.FIXED_WINDOW);
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        Instant originalWindowStart = now.minusSeconds(20);
        ClientRequestInfo clientRequestInfo = new ClientRequestInfo("1",2,originalWindowStart);

        RateLimitResponse response = strategy.processRequest(clientRequestInfo,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(3,clientRequestInfo.getCurrentRequestCount());
        assertEquals(originalWindowStart,clientRequestInfo.getWindowStartTime());
    }

    @Test
    void shouldRejectRequestWhenRequestCountEqualsLimit(){
        RateLimitPolicy policy = new RateLimitPolicy(3, Duration.ofMinutes(1), AlgorithmType.FIXED_WINDOW);
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        Instant originalWindowStart = now.minusSeconds(20);
        ClientRequestInfo clientRequestInfo = new ClientRequestInfo("1",3,originalWindowStart);

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> strategy.processRequest(clientRequestInfo, policy, now)
        );
        assertEquals(3,clientRequestInfo.getCurrentRequestCount());
        assertEquals(originalWindowStart,clientRequestInfo.getWindowStartTime());
        assertEquals(40,exception.getRetryAfterSeconds());
    }

    @Test
    void shouldStartNewWindowWhenWindowExpires(){
        RateLimitPolicy policy = new RateLimitPolicy(3, Duration.ofMinutes(1), AlgorithmType.FIXED_WINDOW);
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        Instant originalWindowStart = now.minusSeconds(60);
        ClientRequestInfo clientRequestInfo = new ClientRequestInfo("1",2,originalWindowStart);

        RateLimitResponse response = strategy.processRequest(clientRequestInfo,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(1,clientRequestInfo.getCurrentRequestCount());
        assertEquals(now,clientRequestInfo.getWindowStartTime());
    }

    @Test
    void shouldAllowOnlyMaxRequestsWhenMultipleThreadsAccessSameClient() {
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        Instant originalWindowStart = now.minusSeconds(5);
        RateLimitPolicy policy = new RateLimitPolicy(3,Duration.ofMinutes(1),AlgorithmType.FIXED_WINDOW);
        ClientRequestInfo clientRequestInfo = new ClientRequestInfo("1",0,originalWindowStart);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(10);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for(int i=0;i<10;i++){
            executor.submit(()->{
                try {
                    startLatch.await();
                    strategy.processRequest(clientRequestInfo, policy, now);
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
            assertEquals(7,failureCount.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }
}