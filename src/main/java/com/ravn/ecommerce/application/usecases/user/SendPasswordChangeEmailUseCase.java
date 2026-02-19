package com.ravn.ecommerce.application.useCases.user;

import com.ravn.ecommerce.application.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Use case for sending password change notification emails.
 * Triggered by PasswordChangedEvent after a successful password change.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SendPasswordChangeEmailUseCase {

    private final EmailService emailService;

    public void execute(String email, LocalDateTime occurredOn) {
        log.info("Preparing password change email for {}", email);

        // Prepare template context with user data
        Map<String, Object> context = new HashMap<>();
        context.put("email", email);
        context.put("date", occurredOn.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Send security alert email using Thymeleaf template
        emailService.sendHtmlEmail(email, "Security Alert: Password Changed", "password-changed", context);
    }
}
