package com.ravn.ecommerce.infrastructure.config;

import com.ravn.ecommerce.application.config.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Only active when using local storage
 */
@Configuration
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class StaticResourceConfig implements WebMvcConfigurer {
    private final AppConfig appConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = appConfig.getStorage().getLocal().getUploadDir();
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
