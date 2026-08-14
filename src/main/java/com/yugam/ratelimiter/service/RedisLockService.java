package com.yugam.ratelimiter.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
public class RedisLockService {
    private final RedissonClient redissonClient;

    public RedisLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public RLock getLock(String clientId){
        return redissonClient.getLock(clientId+"lock");
    }
}
