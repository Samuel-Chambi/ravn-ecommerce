package com.ravn.ecommerce.application.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource( properties = {
        "app.jwt.secret=test-secret-key-must-be-at-least-32-characters",
        "app.jwt.expiration=86400000",
        "app.email.from=test@example.com",
        "app.email.host=smtp.test.com",
        "app.email.port=587",
        "app.email.username=test",
        "app.email.password=test",
        "app.storage.type=local",
        "app.storage.local.upload-dir=./test-uploads",
        "app.rate-limit.requests=5",
        "app.rate-limit.window-seconds=3600",
        "app.stripe.api-key=sk_test_123",
        "app.stripe.webhook-secret=whsec_123"
})
public class AppConfigTest {
    @Autowired
    private AppConfig appConfig;
    @Test
    void shouldLoadConfiguration(){
        assertThat(appConfig).isNotNull();
        assertThat(appConfig.getJwt().getSecret()).isEqualTo("test-secret-key-must-be-at-least-32-characters");
        assertThat(appConfig.getEmail().getFrom()).isEqualTo("test@example.com");
        assertThat(appConfig.getStorage().getType()).isEqualTo("local");
        assertThat(appConfig.getRateLimit().getRequests()).isEqualTo(5);
    }
}
