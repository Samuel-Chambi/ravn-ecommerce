package com.ravn.ecommerce.application.usecases.user;

import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.events.PasswordChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;

    @Transactional
    public void execute(User user, String newPassword) {
        log.info("Changing password for user {}", user.getEmail());

        // Encode and update password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedPassword);

        // Note: For future, save the user to the repository here

        // Publish event asynchronously
        PasswordChangedEvent event = new PasswordChangedEvent(user.getId(), user.getEmail());
        eventPublisher.publish(event);

        log.info("Password changed successfully for user {}", user.getEmail());
    }
}
