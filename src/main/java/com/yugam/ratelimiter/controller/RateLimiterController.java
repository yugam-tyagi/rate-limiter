package com.yugam.ratelimiter.controller;

import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.service.RateLimiterService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rate-limit")
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService){
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/{clientId}")
    public RateLimitResponse handleRequest(@PathVariable String clientId){
        return rateLimiterService.handleRequest(clientId);
    }
}
