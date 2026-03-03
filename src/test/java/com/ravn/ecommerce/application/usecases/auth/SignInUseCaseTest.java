package com.ravn.ecommerce.application.usecases.auth;

import com.ravn.ecommerce.application.dto.request.auth.SignInRequest;
import com.ravn.ecommerce.application.dto.response.AuthResponse;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignInUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private SignInUseCase useCase;

    private User buildActiveUser() {
        return new User(1L, "user@test.com", "hashedPassword", UserRole.CLIENT, true, LocalDateTime.now());
    }

    @Test
    @DisplayName("Should sign in successfully with valid credentials")
    void shouldSignInSuccessfully() {
        User user = buildActiveUser();
        SignInRequest request = new SignInRequest("user@test.com", "password123");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(1L, "CLIENT")).thenReturn("jwt-token-123");

        AuthResponse response = useCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("user@test.com");
        assertThat(response.getRole()).isEqualTo("CLIENT");
    }

    @Test
    @DisplayName("Should throw when email does not exist")
    void shouldThrowWhenUserNotFound() {
        SignInRequest request = new SignInRequest("nonexistent@test.com", "password123");

        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Should throw when password is incorrect")
    void shouldThrowWhenPasswordIncorrect() {
        User user = buildActiveUser();
        SignInRequest request = new SignInRequest("user@test.com", "wrongPassword");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }
}
