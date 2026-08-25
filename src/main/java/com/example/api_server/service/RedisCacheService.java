package com.example.api_server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> T get(String key, TypeReference<T> typeRef) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("캐시 사용");
                return objectMapper.readValue(cached, typeRef);
            }
        } catch (Exception e) {
            log.warn("Redis 캐시 파싱 실패: {}", e.getMessage());
        }
        return null;
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            log.warn("Redis 저장 실패: {}", e.getMessage());        }
    }

    public void set(String key, Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json);
        } catch (Exception e) {
            log.warn("Redis 저장 실패: {}", e.getMessage());
        }
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}