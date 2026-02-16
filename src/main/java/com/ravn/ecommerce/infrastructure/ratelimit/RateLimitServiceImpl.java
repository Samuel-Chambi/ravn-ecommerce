package com.ravn.ecommerce.infrastructure.ratelimit;

import com.ravn.ecommerce.application.services.RateLimitService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitServiceImpl implements RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final Bandwidth bandwidth;

    public RateLimitServiceImpl(Bandwidth bandwidth) {
        this.bandwidth = bandwidth;
    }

    @Override
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, this::newBucket);
    }

    private Bucket newBucket(String key) {
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}
