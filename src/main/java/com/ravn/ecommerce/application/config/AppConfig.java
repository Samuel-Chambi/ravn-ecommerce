package com.ravn.ecommerce.application.config;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
@Getter
@Setter
// Class for check correct settings for app configurations
public class AppConfig {
    @NotNull(message = "JWT configuration is required")
    private JwtConfig jwt;
    @NotNull(message = "Email configuration is required")
    private EmailConfig email;
    @NotNull(message = "Storage configuration is required")
    private StorageConfig storage;
    @NotNull(message = "Rate limit configuration is required")
    private RateLimitConfig rateLimit;
    @NotNull(message = "Stripe configuration is required")
    private StripeConfig stripe;
    @NotNull(message = "CORS configuration is required")
    private CorsConfig cors;

    @Getter
    @Setter
    public static class JwtConfig {
        @NotBlank(message = "JWT secret cannot be blank")
        @Size(min = 32, message = "JWT secret must be at least 32 characters")
        private String secret;
        @Min(value = 60000, message = "JWT expiration must be at least 60000ms")
        private Long expiration;
    }

    @Getter
    @Setter
    public static class EmailConfig {
        private boolean enabled = true;
        private String from;
        private String host;
        private Integer port;
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class StorageConfig {
        @NotBlank(message = "Storage type cannot be blank")
        @Pattern(regexp = "local|s3", message = "Storage type must be 'local' or 's3'")
        private String type;
        private LocalStorageConfig local;
        private S3StorageConfig s3;

        @Getter
        @Setter
        public static class LocalStorageConfig {
            @NotBlank(message = "Upload directory cannot be blank")
            private String uploadDir;
            @NotBlank(message = "Base URL cannot be blank")
            private String baseUrl;
        }

        @Getter
        @Setter
        public static class S3StorageConfig {
            private String bucket;
            private String region;
            private String endpoint; // For LocalStack
            private String accessKey;
            private String secretKey;
        }
    }

    @Getter
    @Setter
    public static class RateLimitConfig {
        @Min(value = 1, message = "Rate limit requests must be at least 1")
        @Max(value = 100, message = "Rate limit requests cannot exceed 100")
        private Integer requests;

        @Min(value = 60, message = "Rate limit window must be at least 60 seconds")
        private Integer windowSeconds;
    }

    @Getter
    @Setter
    public static class StripeConfig {
        @NotBlank(message = "Stripe API key cannot be blank")
        private String apiKey;
        @NotBlank(message = "Stripe webhook secret cannot be blank")
        private String webhookSecret;
    }

    @Getter
    @Setter
    public static class CorsConfig {
        @NotNull(message = "Allowed origins cannot be null")
        private List<String> allowedOrigins;
        @NotNull(message = "Allowed methods cannot be null")
        private List<String> allowedMethods;
        @NotNull(message = "Allowed headers cannot be null")
        private List<String> allowedHeaders;
    }
}
