package com.ravn.ecommerce.infrastructure.config;

import com.ravn.ecommerce.application.config.AppConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/*
* Only active using localstack storage
*/
@Configuration
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3ClientConfig {

        @Bean
        @Profile("localstack")
        public S3Client s3ClientLocalStack(AppConfig appConfig) {
                var s3Config = appConfig.getStorage().getS3();

                return S3Client.builder()
                                .endpointOverride(URI.create(s3Config.getEndpoint()))
                                .region(Region.of(s3Config.getRegion()))
                                .credentialsProvider(StaticCredentialsProvider.create(
                                                AwsBasicCredentials.create("test", "test")))
                                .serviceConfiguration(S3Configuration.builder()
                                                .pathStyleAccessEnabled(true) // Required for LocalStack
                                                .build())
                                .build();
        }
}
