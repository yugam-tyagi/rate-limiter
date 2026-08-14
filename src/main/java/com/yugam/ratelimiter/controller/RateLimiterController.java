package com.yugam.ratelimiter.controller;

import com.yugam.ratelimiter.dto.RateLimitRequest;
import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.model.ClientConfiguration;
import com.yugam.ratelimiter.repository.RedisTestRepository;
import com.yugam.ratelimiter.service.RateLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/rate-limit")
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;
    private final RedisTestRepository redisTestRepository;

    public RateLimiterController(RateLimiterService rateLimiterService,RedisTestRepository redisTestRepository){
        this.rateLimiterService = rateLimiterService;
        this.redisTestRepository = redisTestRepository;
    }

    @PostMapping("/config/create")
    public void createClient(@RequestBody ClientConfiguration request){
        rateLimiterService.createClient(request);
    }

    @GetMapping("/config/{clientId}")
    public ClientConfiguration getClient(@PathVariable String clientId){
        return rateLimiterService.getClient(clientId);
    }

    @PostMapping("/check")
    public RateLimitResponse handleRequest(@RequestBody RateLimitRequest request){
        return rateLimiterService.handleRequest(request);
    }
}
