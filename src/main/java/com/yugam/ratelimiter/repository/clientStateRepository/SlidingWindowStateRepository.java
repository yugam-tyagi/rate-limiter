package com.yugam.ratelimiter.repository.clientStateRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.state.SlidingWindowState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.ArrayDeque;

@Repository
public class SlidingWindowStateRepository implements RedisRateLimitStateRepository<SlidingWindowState> {
    private final RedisTemplate<String,Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public SlidingWindowStateRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.SLIDING_WINDOW;
    }

    @Override
    public SlidingWindowState getState(String clientId) {
        try{
            String key = clientId+"state";
            String value = redisTemplate.opsForValue().get(key).toString();
            SlidingWindowState state = objectMapper.readValue(value, SlidingWindowState.class);
            return state;
        }
        catch (JsonProcessingException ex){
            throw new RuntimeException("Failed to deserialize client state", ex);
        }
    }

    @Override
    public void saveState(String clientId, SlidingWindowState state) {
        try{
            String key = clientId+"state";
            String value = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(key,value);
        }
        catch (JsonProcessingException ex){
            throw new RuntimeException("Failed to serialize client state", ex);
        }
    }

    @Override
    public void initializeState(String clientId) {
        try{
            SlidingWindowState state = new SlidingWindowState(new ArrayDeque<>());
            String key = clientId+"state";
            String value = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(key,value);
        }
        catch (JsonProcessingException ex){
            throw new RuntimeException("Failed to serialize client state", ex);
        }
    }
}
