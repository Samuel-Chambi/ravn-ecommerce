package com.ravn.ecommerce.application.usecases.user;

import com.ravn.ecommerce.application.services.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SendPasswordChangeEmailUseCaseTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SendPasswordChangeEmailUseCase useCase;

    @Test
    @DisplayName("Should send password change email successfully")
    void shouldSendPasswordChangeEmail() {
        String email = "user@test.com";
        LocalDateTime occurredOn = LocalDateTime.of(2025, 1, 15, 10, 30, 0);

        useCase.execute(email, occurredOn);

        verify(emailService).sendHtmlEmail(
                eq(email),
                eq("Security Alert: Password Changed"),
                eq("password-changed"),
                argThat(context -> context.containsKey("email") && context.containsKey("date"))
        );
    }

    @Test
    @DisplayName("Should include formatted date in email context")
    void shouldIncludeFormattedDateInContext() {
        String email = "test@example.com";
        LocalDateTime occurredOn = LocalDateTime.of(2025, 6, 20, 14, 0, 0);

        useCase.execute(email, occurredOn);

        verify(emailService).sendHtmlEmail(
                eq(email),
                anyString(),
                eq("password-changed"),
                argThat(context -> {
                    String date = (String) context.get("date");
                    return date != null && date.contains("2025-06-20");
                })
        );
    }
}
