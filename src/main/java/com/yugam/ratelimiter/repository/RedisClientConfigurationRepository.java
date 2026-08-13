package com.yugam.ratelimiter.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.policy.RateLimitPolicy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.Map;

@Repository
public class RedisClientConfigurationRepository {
    private final RedisTemplate<String,Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisClientConfigurationRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(String clientId, AlgorithmType algorithmType, RateLimitPolicy policy){
        try{
            redisTemplate.opsForHash().putAll(
                    clientId,
                    Map.of(
                            "clientId", clientId,
                            "algorithmType", algorithmType.name(),
                            "policy", objectMapper.writeValueAsString(policy)
                    )
            );
        }
        catch(JsonProcessingException ex){
            throw new RuntimeException("Failed to serialize rate limit policy", ex);
        }
    }
}
