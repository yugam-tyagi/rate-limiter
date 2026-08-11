package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.LeakyBucketClientInfo;
import com.yugam.ratelimiter.model.LeakyBucketPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LeakyBucketStrategyTest {
    private RateLimiterStrategy strategy;

    @BeforeEach
    void setup(){
        strategy = new LeakyBucketStrategy();
    }

    @Test
    void shouldAllowRequestWhenBucketIsNotFull(){
        Instant now = Instant.now();
        LeakyBucketPolicy policy = new LeakyBucketPolicy(5,2);
        LeakyBucketClientInfo clientInfo = new LeakyBucketClientInfo("1",3, now);

        RateLimitResponse response = strategy.processRequest(clientInfo,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(1,response.getRemainingRequests());
        assertEquals(4,clientInfo.getCurrentRequestCount());
        assertEquals(now,clientInfo.getLastLeakTime());
    }

    @Test
    void shouldRejectRequestWhenBucketIsFull(){
        Instant now = Instant.now();
        LeakyBucketPolicy policy = new LeakyBucketPolicy(5,2);
        LeakyBucketClientInfo clientInfo = new LeakyBucketClientInfo("1",5, now);

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                ()->strategy.processRequest(clientInfo,policy,now)
        );

        assertEquals(60,exception.getRetryAfterSeconds());
        assertEquals(5,clientInfo.getCurrentRequestCount());
        assertEquals(now,clientInfo.getLastLeakTime());
    }

    @Test
    void shouldLeakRequestsBasedOnElapsedMinutes(){
        Instant now = Instant.now();
        LeakyBucketPolicy policy = new LeakyBucketPolicy(5,2);
        LeakyBucketClientInfo clientInfo = new LeakyBucketClientInfo("1",5, now.minusSeconds(120));

        RateLimitResponse response = strategy.processRequest(clientInfo,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(3,response.getRemainingRequests());
        assertEquals(2,clientInfo.getCurrentRequestCount());
        assertEquals(now,clientInfo.getLastLeakTime());
    }

    @Test
    void shouldEmptyBucketAfterSufficientTime(){
        Instant now = Instant.now();
        LeakyBucketPolicy policy = new LeakyBucketPolicy(5,2);
        LeakyBucketClientInfo clientInfo = new LeakyBucketClientInfo("1",5, now.minusSeconds(300));

        RateLimitResponse response = strategy.processRequest(clientInfo,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(4,response.getRemainingRequests());
        assertEquals(1,clientInfo.getCurrentRequestCount());
        assertEquals(now,clientInfo.getLastLeakTime());
    }

    @Test
    void shouldAllowOnlyBucketCapacityRequestsWhenMultipleThreadsAccessSameClient(){
        Instant now = Instant.now();
        LeakyBucketPolicy policy = new LeakyBucketPolicy(5,2);
        LeakyBucketClientInfo clientInfo = new LeakyBucketClientInfo("1",1, now);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(10);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for(int i=0;i<10;i++){
            executor.submit(()->{
                try{
                    startLatch.await();
                    strategy.processRequest(clientInfo,policy,now);
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
