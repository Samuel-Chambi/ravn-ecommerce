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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ChangePasswordUseCase useCase;

    private User buildUser() {
        return User.builder()
                .id(1L)
                .email("user@test.com")
                .passwordHash("oldHashedPassword")
                .role(UserRole.CLIENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should change password and publish event")
    void shouldChangePasswordAndPublishEvent() {
        User user = buildUser();
        String newPassword = "newSecurePassword123";

        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedHash");

        useCase.execute(user, newPassword);

        verify(passwordEncoder).encode(newPassword);
        verify(eventPublisher).publish(any(PasswordChangedEvent.class));
    }

    @Test
    @DisplayName("Should encode password before changing")
    void shouldEncodePasswordBeforeChanging() {
        User user = buildUser();
        String newPassword = "anotherPassword";

        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

        useCase.execute(user, newPassword);

        verify(passwordEncoder).encode(eq(newPassword));
    }
}
