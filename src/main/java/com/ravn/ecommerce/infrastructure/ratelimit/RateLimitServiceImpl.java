package com.ravn.ecommerce.infrastructure.ratelimit;

import com.ravn.ecommerce.application.services.RateLimitService;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import com.ravn.ecommerce.application.config.AppConfig;
import org.springframework.stereotype.Service;

@Service
public class RateLimitServiceImpl implements RateLimitService {

    private final RedissonClient redissonClient;
    private final AppConfig appConfig;

    public RateLimitServiceImpl(RedissonClient redissonClient, AppConfig appConfig) {
        this.redissonClient = redissonClient;
        this.appConfig = appConfig;
    }

    @Override
    public RRateLimiter getRateLimiter(String key) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        long requests = appConfig.getRateLimit().getRequests();
        long windowSeconds = appConfig.getRateLimit().getWindowSeconds();

        rateLimiter.trySetRate(RateType.OVERALL, requests, windowSeconds, RateIntervalUnit.SECONDS);
        return rateLimiter;
    }
}
