package com.yugam.ratelimiter.service;

import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.ClientRequestInfo;
import com.yugam.ratelimiter.model.RateLimitPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.time.Instant;

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

        boolean result = strategy.processRequest(clientRequestInfo,policy,now);

        assertTrue(result);
        assertEquals(3,clientRequestInfo.getCurrentRequestCount());
        assertEquals(originalWindowStart,clientRequestInfo.getWindowStartTime());
    }

    @Test
    void shouldRejectRequestWhenRequestCountEqualsLimit(){
        RateLimitPolicy policy = new RateLimitPolicy(3, Duration.ofMinutes(1), AlgorithmType.FIXED_WINDOW);
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        Instant originalWindowStart = now.minusSeconds(20);
        ClientRequestInfo clientRequestInfo = new ClientRequestInfo("1",3,originalWindowStart);

        boolean result = strategy.processRequest(clientRequestInfo,policy,now);

        assertFalse(result);
        assertEquals(3,clientRequestInfo.getCurrentRequestCount());
        assertEquals(originalWindowStart,clientRequestInfo.getWindowStartTime());
    }

    @Test
    void shouldStartNewWindowWhenWindowExpires(){
        RateLimitPolicy policy = new RateLimitPolicy(3, Duration.ofMinutes(1), AlgorithmType.FIXED_WINDOW);
        Instant now = Instant.parse("2026-07-28T10:00:00Z");
        Instant originalWindowStart = now.minusSeconds(60);
        ClientRequestInfo clientRequestInfo = new ClientRequestInfo("1",2,originalWindowStart);

        boolean result = strategy.processRequest(clientRequestInfo,policy,now);

        assertTrue(result);
        assertEquals(1,clientRequestInfo.getCurrentRequestCount());
        assertEquals(now,clientRequestInfo.getWindowStartTime());
    }
}