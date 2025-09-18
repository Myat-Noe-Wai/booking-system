package com.bookingsystem.application.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Try to acquire lock for given key with TTL (seconds).
     * Returns a token if acquired, otherwise null.
     */
    public String tryLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, token, ttl.getSeconds(), TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) return token;
        return null;
    }

    /**
     * Release lock only if token matches (safe unlock).
     */
    public boolean releaseLock(String key, String token) {
        String current = redisTemplate.opsForValue().get(key);
        if (token != null && token.equals(current)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}

