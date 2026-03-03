package com.ravn.ecommerce.application.usecases.auth;

import com.ravn.ecommerce.application.dto.request.auth.SignUpRequest;
import com.ravn.ecommerce.application.dto.response.AuthResponse;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.domain.exceptions.DuplicateResourceException;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import com.ravn.ecommerce.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpUseCaseTest {

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String JWT_TOKEN = "jwt-token-123";
    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private SignUpUseCase signUpUseCase;

    @Test
    @DisplayName("Should sign up successfully as CLIENT by default")
    void shouldSignUpSuccessfullyAsClient() {
        SignUpRequest request = new SignUpRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .id(USER_ID)
                    .email(user.getEmail())
                    .passwordHash(user.getPasswordHash())
                    .role(user.getRole())
                    .active(user.isActive())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        });
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn(JWT_TOKEN);

        AuthResponse response = signUpUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(JWT_TOKEN);
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getRole()).isEqualTo("CLIENT");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should sign up successfully as MANAGER when role is specified")
    void shouldSignUpAsManager() {
        SignUpRequest request = new SignUpRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setRole("MANAGER");

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .id(USER_ID)
                    .email(user.getEmail())
                    .passwordHash(user.getPasswordHash())
                    .role(user.getRole())
                    .active(user.isActive())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        });
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn(JWT_TOKEN);

        AuthResponse response = signUpUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo("MANAGER");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowWhenEmailAlreadyExists() {
        SignUpRequest request = new SignUpRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> signUpUseCase.execute(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }
}
