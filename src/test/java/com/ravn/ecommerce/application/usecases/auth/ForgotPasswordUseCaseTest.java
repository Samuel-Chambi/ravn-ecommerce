package com.ravn.ecommerce.application.usecases.auth;

import com.ravn.ecommerce.application.dto.request.auth.ForgotPasswordRequest;
import com.ravn.ecommerce.application.dto.response.ForgotPasswordResponse;
import com.ravn.ecommerce.application.repositories.PasswordResetRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.services.EmailService;
import com.ravn.ecommerce.domain.model.user.PasswordReset;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordUseCaseTest {

    private static final String EMAIL = "user@example.com";
    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ForgotPasswordUseCase forgotPasswordUseCase;

    private User buildUser() {
        return User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .passwordHash("hashed")
                .role(UserRole.CLIENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should generate reset token and send email when user exists")
    void shouldGenerateResetTokenWhenUserExists() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(passwordResetRepository.save(any(PasswordReset.class))).thenAnswer(i -> i.getArgument(0));

        ForgotPasswordResponse response = forgotPasswordUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getResetToken()).isNotNull();
        assertThat(response.getMessage()).contains("Reset token generated");
        verify(passwordResetRepository).invalidateAllForUser(USER_ID);
        verify(passwordResetRepository).save(any(PasswordReset.class));
        verify(emailService).sendHtmlEmail(eq(EMAIL), eq("Password Reset Request"), eq("reset-password"), anyMap());
    }

    @Test
    @DisplayName("Should return generic message when email is not registered (silent fail)")
    void                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              shouldReturnGenericMessageWhenEmailNotRegistered() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("unknown@example.com");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        ForgotPasswordResponse response = forgotPasswordUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getResetToken()).isNull();
        assertThat(response.getMessage()).contains("If that email is registered");
        verify(passwordResetRepository, never()).save(any());
        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString(), anyMap());
    }
}
