package com.ravn.ecommerce.application.usecases.auth;

import com.ravn.ecommerce.application.dto.request.auth.ResetPasswordRequest;
import com.ravn.ecommerce.application.repositories.PasswordResetRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.services.EmailService;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.user.PasswordReset;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseTest {

    private static final String TOKEN = "valid-reset-token";
    private static final String NEW_PASSWORD = "newPassword123";
    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ResetPasswordUseCase resetPasswordUseCase;

    private ResetPasswordRequest buildRequest() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(TOKEN);
        request.setNewPassword(NEW_PASSWORD);
        return request;
    }

    private PasswordReset buildValidReset() {
        return new PasswordReset(1L, false, LocalDateTime.now().plusMinutes(15), TOKEN, USER_ID);
    }

    private User buildUser() {
        return User.builder()
                .id(USER_ID)
                .email("user@example.com")
                .passwordHash("old-hash")
                .role(UserRole.CLIENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should reset password successfully with valid token")
    void shouldResetPasswordSuccessfully() {
        PasswordReset reset = buildValidReset();
        User user = buildUser();

        when(passwordResetRepository.findByTokenHash(TOKEN)).thenReturn(Optional.of(reset));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("new-encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordResetRepository.save(any(PasswordReset.class))).thenReturn(reset);

        String result = resetPasswordUseCase.execute(buildRequest());

        assertThat(result).isEqualTo("Password updated successfully");
        verify(userRepository).save(any(User.class));
        verify(passwordResetRepository).save(any(PasswordReset.class));
        verify(emailService).sendHtmlEmail(eq("user@example.com"), eq("Password Changed Successfully"), eq("password-changed"), anyMap());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when token is invalid")
    void shouldThrowWhenTokenIsInvalid() {
        when(passwordResetRepository.findByTokenHash(TOKEN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resetPasswordUseCase.execute(buildRequest()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Invalid or expired token");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when token is expired")
    void shouldThrowWhenTokenIsExpired() {
        PasswordReset expiredReset = new PasswordReset(1L, false, LocalDateTime.now().minusMinutes(1), TOKEN, USER_ID);

        when(passwordResetRepository.findByTokenHash(TOKEN)).thenReturn(Optional.of(expiredReset));

        assertThatThrownBy(() -> resetPasswordUseCase.execute(buildRequest()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Token has expired or already been used");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when token already used")
    void shouldThrowWhenTokenAlreadyUsed() {
        PasswordReset usedReset = new PasswordReset(1L, true, LocalDateTime.now().plusMinutes(15), TOKEN, USER_ID);

        when(passwordResetRepository.findByTokenHash(TOKEN)).thenReturn(Optional.of(usedReset));

        assertThatThrownBy(() -> resetPasswordUseCase.execute(buildRequest()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Token has expired or already been used");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found")
    void shouldThrowWhenUserNotFound() {
        PasswordReset reset = buildValidReset();

        when(passwordResetRepository.findByTokenHash(TOKEN)).thenReturn(Optional.of(reset));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resetPasswordUseCase.execute(buildRequest()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
