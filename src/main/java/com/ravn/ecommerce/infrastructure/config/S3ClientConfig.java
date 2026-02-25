package com.ravn.ecommerce.infrastructure.config;

import com.ravn.ecommerce.application.config.AppConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3ClientConfig {

        @Bean
        public S3Client s3Client(AppConfig appConfig) {
                var s3Config = appConfig.getStorage().getS3();

                AwsBasicCredentials credentials = AwsBasicCredentials.create(
                                s3Config.getAccessKey(),
                                s3Config.getSecretKey());

                return S3Client.builder()
                                .region(Region.of(s3Config.getRegion()))
                                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                                .build();
        }
}
