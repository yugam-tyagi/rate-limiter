package com.yugam.ratelimiter.controller;

import com.yugam.ratelimiter.dto.ClientConfigurationRequest;
import com.yugam.ratelimiter.dto.RateLimitResponse;
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
    public void createClient(@RequestBody ClientConfigurationRequest request){
        rateLimiterService.createClient(request);
    }

    @PostMapping("/{key}/{value}")
    public void saveToRedis(@PathVariable String key, @PathVariable Object value){
        redisTestRepository.save(key,value);
    }

    @GetMapping("/{key}")
    public Object getFromRedis(@PathVariable String key){
        return redisTestRepository.get(key);
    }

    @PostMapping("/{clientId}")
    public RateLimitResponse handleRequest(@PathVariable String clientId){
        log.info("Received rate limit request for clientId: {}",clientId);
        return rateLimiterService.handleRequest(clientId);
    }
}
