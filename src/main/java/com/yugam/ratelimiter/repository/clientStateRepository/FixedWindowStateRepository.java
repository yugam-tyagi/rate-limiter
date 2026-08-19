package com.yugam.ratelimiter.repository.clientStateRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.model.state.FixedWindowState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FixedWindowStateRepository implements RedisRateLimitStateRepository<FixedWindowState> {
    private final RedisTemplate<String,Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public FixedWindowStateRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public FixedWindowState getState(String clientId){
        try{
            String key = clientId+"state";
            String value = redisTemplate.opsForValue().get(key).toString();
            FixedWindowState state = objectMapper.readValue(value, FixedWindowState.class);
            return state;
        }
        catch (JsonProcessingException ex){
            throw new RuntimeException("Failed to deserialize client state", ex);
        }
    }

    @Override
    public void saveState(String clientId, FixedWindowState state){
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
            FixedWindowState state = new FixedWindowState(0,null);
            String key = clientId+"state";
            String value = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(key,value);
        }
        catch (JsonProcessingException ex){
            throw new RuntimeException("Failed to serialize client state", ex);
        }
    }

    @Override
    public AlgorithmType getAlgorithmType() {
        return AlgorithmType.FIXED_WINDOW;
    }
}
