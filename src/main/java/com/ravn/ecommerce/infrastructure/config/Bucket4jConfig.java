package com.ravn.ecommerce.infrastructure.config;

import com.ravn.ecommerce.application.config.AppConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for rate limiting using Bucket4j.
 * Creates Bandwidth bean based on configurable values from application.yml.
 */
@Configuration
@RequiredArgsConstructor
public class Bucket4jConfig {

    private final AppConfig appConfig;

    /**
     * Creates Bandwidth bean for rate limiting.
     * Uses values from app.rate-limit in application.yml.
     */
    @Bean
    public Bandwidth defaultBandwidth() {
        AppConfig.RateLimitConfig rateLimitConfig = appConfig.getRateLimit();

        return Bandwidth.classic(
                rateLimitConfig.getRequests(),
                Refill.greedy(
                        rateLimitConfig.getRequests(),
                        Duration.ofSeconds(rateLimitConfig.getWindowSeconds())));
    }
}
