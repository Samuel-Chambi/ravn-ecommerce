package com.ravn.ecommerce.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public void banToken(String token, long timeToLiveMillis) {
        if (timeToLiveMillis > 0) {
            log.info("Banning JWT token. TTL: {} ms", timeToLiveMillis);
            redisTemplate.opsForValue().set(token, "banned", timeToLiveMillis, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isTokenBanned(String token) {
        return redisTemplate.hasKey(token);
    }
}
