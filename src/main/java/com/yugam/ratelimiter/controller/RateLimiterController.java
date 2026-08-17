package com.yugam.ratelimiter.controller;

import com.yugam.ratelimiter.dto.RateLimitRequest;
import com.yugam.ratelimiter.dto.RateLimitResponse;
import com.yugam.ratelimiter.model.ClientConfiguration;
import com.yugam.ratelimiter.repository.RedisTestRepository;
import com.yugam.ratelimiter.service.RateLimiterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/rate-limit")
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService,RedisTestRepository redisTestRepository){
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/config/create")
    public void createClient(@Valid @RequestBody ClientConfiguration request){
        rateLimiterService.createClient(request);
    }

    @GetMapping("/config/{clientId}")
    public ClientConfiguration getClient(@PathVariable @NotBlank(message = "ClientId can't be empty.") String clientId){
        return rateLimiterService.getClient(clientId);
    }

    @DeleteMapping("/reset-state")
    public ResponseEntity<Void> resetState(@Valid @RequestBody RateLimitRequest request){
        rateLimiterService.resetState(request);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/check")
    public RateLimitResponse handleRequest(@Valid @RequestBody RateLimitRequest request){
        return rateLimiterService.handleRequest(request);
    }
}
