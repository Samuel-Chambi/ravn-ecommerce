package com.ravn.ecommerce.application.usecases.user;

import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import com.ravn.ecommerce.domain.model.user.events.PasswordChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordChangeUseCaseTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ChangePasswordUseCase useCase;

    @Test
    @DisplayName("Should encode password and update user")
    void shouldEncodeAndUpdatePassword() {
        User user = new User(1L, "test@example.com", "oldHash", UserRole.CLIENT, true, LocalDateTime.now());
        String newPassword = "newSecurePassword123";

        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedHash");

        useCase.execute(user, newPassword);

        assertThat(user.getPasswordHash()).isEqualTo("newEncodedHash");
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    @DisplayName("Should publish PasswordChangedEvent after password change")
    void shouldPublishPasswordChangedEvent() {
        User user = new User(1L, "test@example.com", "oldHash", UserRole.CLIENT, true, LocalDateTime.now());

        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        useCase.execute(user, "newPass");

        verify(eventPublisher).publish(any(PasswordChangedEvent.class));
    }
}
