package com.ravn.ecommerce.application.services;

import org.redisson.api.RRateLimiter;

public interface RateLimitService {
    RRateLimiter getRateLimiter(String key);
}
