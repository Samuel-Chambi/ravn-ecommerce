package com.ravn.ecommerce.infrastructure.email;

import com.ravn.ecommerce.application.services.EmailService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@SpringBootTest
public class EmailServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    public void testSendHtmlEmail() {
        // Mock MimeMessage
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Prepare context
        Map<String, Object> context = new HashMap<>();
        context.put("email", "test@example.com");
        context.put("date", "2023-10-27 10:00:00");

        // Execute
        emailService.sendHtmlEmail("test@example.com", "Test Subject", "password-changed", context);

        // Verify that send was called asynchronously
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> verify(javaMailSender, times(1)).send(mimeMessage));
    }
}
