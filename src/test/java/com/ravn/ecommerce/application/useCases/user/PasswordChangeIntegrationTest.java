package com.ravn.ecommerce.application.useCases.user;

import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.services.EmailService;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PasswordChangeIntegrationTest {

    @Autowired
    private ChangePasswordUseCase changePasswordUseCase;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    public void testPasswordChangeFlow() {
        // Mock JavaMailSender
        when(javaMailSender.createMimeMessage()).thenReturn(mock(jakarta.mail.internet.MimeMessage.class));

        // Create a test user
        User user = new User(1L, "test@example.com", "oldPasswordHash", UserRole.CLIENT, true, LocalDateTime.now());
        String newPassword = "newSecurePassword123";

        // Execute password change
        changePasswordUseCase.execute(user, newPassword);

        // Verify password was changed
        assertTrue(passwordEncoder.matches(newPassword, user.getPasswordHash()),
                "Password should be updated and match the new password");

        // Verify email was sent asynchronously
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> verify(javaMailSender, times(1)).send(any(jakarta.mail.internet.MimeMessage.class)));
    }
}
