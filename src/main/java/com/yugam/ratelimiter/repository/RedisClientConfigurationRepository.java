package com.yugam.ratelimiter.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yugam.ratelimiter.enums.AlgorithmType;
import com.yugam.ratelimiter.exception.exceptions.ClientNotFoundException;
import com.yugam.ratelimiter.model.ClientConfiguration;
import com.yugam.ratelimiter.model.PolicyData;
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

    public void save(String clientId, AlgorithmType algorithmType, PolicyData data){
        try{
            redisTemplate.opsForHash().putAll(
                    clientId,
                    Map.of(
                            "clientId", clientId,
                            "algorithmType", algorithmType.name(),
                            "policyData", objectMapper.writeValueAsString(data)
                    )
            );
        }
        catch (JsonProcessingException exception) {
            throw new RuntimeException("Failed to serialize policy data", exception);
        }
    }

    public ClientConfiguration get(String clientId) {
        try{
            Map<Object,Object> clientData = redisTemplate.opsForHash().entries(clientId);

            if (clientData.isEmpty()) {
                throw new ClientNotFoundException(clientId);
            }

            String algorithm = (String) clientData.get("algorithmType");
            AlgorithmType algorithmType = AlgorithmType.valueOf(algorithm);
            String data = (String) clientData.get("policyData");
            PolicyData policyData = objectMapper.readValue(data, PolicyData.class);

            ClientConfiguration clientConfiguration = new ClientConfiguration(clientId,algorithmType,policyData);

            return clientConfiguration;
        }
        catch (JsonProcessingException exception) {
            throw new RuntimeException("Failed to deserialize policy data", exception);
        }
    }
}
