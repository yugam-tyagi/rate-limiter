package com.yugam.ratelimiter.controller;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.service.RateLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rate-limit")
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService){
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/{clientId}")
    public RateLimitResponse handleRequest(@PathVariable String clientId){
        log.info("Received rate limit request for clientId: {}",clientId);
        return rateLimiterService.handleRequest(clientId);
    }
}
