package com.yugam.ratelimiter.strategy;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.exception.RateLimitExceededException;
import com.yugam.ratelimiter.model.TokenBucketClientInfo;
import com.yugam.ratelimiter.model.TokenBucketPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TokenBucketStrategyTest {
    private RateLimiterStrategy strategy;

    @BeforeEach
    void setup(){
        strategy = new TokenBucketStrategy();
    }

    @Test
    void shouldAllowRequestWhenTokensAreAvailable(){
        TokenBucketPolicy policy = new TokenBucketPolicy(10,2);
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        TokenBucketClientInfo clientRequestInfo = new TokenBucketClientInfo("1",2,now);

        RateLimitResponse response = strategy.processRequest(clientRequestInfo,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(1,clientRequestInfo.getAvailableTokens());
        assertEquals(1,response.getRemainingRequests());
        assertEquals(now,clientRequestInfo.getLastRefillTime());
    }

    @Test
    void shouldRejectRequestWhenNoTokensAreAvailable(){
        TokenBucketPolicy policy = new TokenBucketPolicy(10,2);
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        TokenBucketClientInfo clientRequestInfo = new TokenBucketClientInfo("1",0,now);

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> strategy.processRequest(clientRequestInfo, policy, now)
        );

        assertEquals(1,exception.getRetryAfterSeconds());
        assertEquals(0,clientRequestInfo.getAvailableTokens());
        assertEquals(now,clientRequestInfo.getLastRefillTime());
    }

    @Test
    void shouldAllowRequestAfterPartialTokenRefill(){
        TokenBucketPolicy policy = new TokenBucketPolicy(10,2);
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        TokenBucketClientInfo clientRequestInfo = new TokenBucketClientInfo("1",0,now.minusSeconds(20));

        RateLimitResponse response = strategy.processRequest(clientRequestInfo,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(9,clientRequestInfo.getAvailableTokens());
        assertEquals(9,response.getRemainingRequests());
        assertEquals(now,clientRequestInfo.getLastRefillTime());
    }

    @Test
    void shouldNotExceedBucketCapacityWhenRefilling(){
        TokenBucketPolicy policy = new TokenBucketPolicy(10,2);
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        TokenBucketClientInfo clientRequestInfo = new TokenBucketClientInfo("1",0,now.minusSeconds(10));

        RateLimitResponse response = strategy.processRequest(clientRequestInfo,policy,now);

        assertTrue(response.isAllowed());
        assertEquals(9,clientRequestInfo.getAvailableTokens());
        assertEquals(9,response.getRemainingRequests());
        assertEquals(now,clientRequestInfo.getLastRefillTime());
    }

    @Test
    void shouldAllowOnlyBucketCapacityRequestsWhenMultipleThreadsAccessSameClient(){
        TokenBucketPolicy policy = new TokenBucketPolicy(10,2);
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        TokenBucketClientInfo clientRequestInfo = new TokenBucketClientInfo("1",0,now.minusSeconds(2));

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finsihLatch = new CountDownLatch(10);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for(int i=0;i<10;i++){
            executor.submit(()->{
                try{
                    startLatch.await();
                    strategy.processRequest(clientRequestInfo,policy,now);
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
            assertEquals(0,clientRequestInfo.getAvailableTokens());
            assertEquals(now,clientRequestInfo.getLastRefillTime());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        finally {
            executor.shutdown();
        }
    }
}
