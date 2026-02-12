package com.ravn.ecommerce.application.services;

import io.github.bucket4j.Bucket;

public interface RateLimitService {
    Bucket resolveBucket(String key);
}
