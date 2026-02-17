package com.ravn.ecommerce.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfig {

    /**
     * Provides a dummy JavaMailSender bean when email service is disabled.
     * This prevents injection failures in EmailServiceImpl.
     */
    @Bean
    @ConditionalOnProperty(name = "app.email.enabled", havingValue = "false")
    public JavaMailSender javaMailSender() {
        return new JavaMailSenderImpl();
    }
}
