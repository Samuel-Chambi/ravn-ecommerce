package com.ravn.ecommerce.presentation.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ravn.ecommerce.application.dto.request.auth.*;
import com.ravn.ecommerce.application.dto.response.ForgotPasswordResponse;
import com.ravn.ecommerce.application.dto.response.AuthResponse;
import com.ravn.ecommerce.application.usecases.auth.*;
import com.ravn.ecommerce.application.services.RateLimitService;
import com.ravn.ecommerce.domain.exceptions.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RRateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, logout, and password recovery")
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final SignInUseCase signInUseCase;
    private final SignOutUseCase signOutUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    private final RateLimitService rateLimitService;

    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details. Returns an authentication token upon successful registration.")
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(
            @RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(signUpUseCase.execute(request));
    }

    @Operation(summary = "User login", description = "Authenticates a user with email and password, returning an access token to be used for authorized requests.")
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(
            @RequestBody @Valid SignInRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(signInUseCase.execute(request));
    }

    @Operation(summary = "User logout", description = "Invalidates the user's current access token, signing them out of the application.")
    @PostMapping("/signout")
    public ResponseEntity<String> signOut(@RequestHeader("Authorization") String authHeader) {
        signOutUseCase.execute(authHeader);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Request password reset", description = "Sends a password reset link/token to the user's email address if it exists in the system. Rate-limited to prevent abuse.")
    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = httpRequest.getRemoteAddr();
        RRateLimiter rateLimiter = rateLimitService.getRateLimiter("rate_limit:forgot_pwd:" + clientIp);

        if (!rateLimiter.tryAcquire(1)) {
            throw new TooManyRequestsException("Too many reset attempts. Please wait before trying again.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(forgotPasswordUseCase.execute(request));
    }

    @Operation(summary = "Reset password", description = "Resets the user's password using the token received via the forgot-password endpoint.")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(resetPasswordUseCase.execute(request));
    }
}
