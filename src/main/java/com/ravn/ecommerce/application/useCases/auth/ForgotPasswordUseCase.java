package com.ravn.ecommerce.application.usecases.auth;

import com.ravn.ecommerce.application.dto.request.auth.ForgotPasswordRequest;
import com.ravn.ecommerce.application.dto.response.ForgotPasswordResponse;
import com.ravn.ecommerce.application.repositories.PasswordResetRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.services.EmailService;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.model.user.PasswordReset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForgotPasswordUseCase implements UseCase<ForgotPasswordRequest, ForgotPasswordResponse> {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public ForgotPasswordResponse execute(ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());

        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    // Invalidate all previous tokens for this user
                    passwordResetRepository.invalidateAllForUser(user.getId());

                    // Generate a secure random token
                    byte[] tokenBytes = new byte[32];
                    new SecureRandom().nextBytes(tokenBytes);
                    String plainToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

                    PasswordReset reset = new PasswordReset(
                            null,
                            false,
                            LocalDateTime.now().plusMinutes(15),
                            plainToken,
                            user.getId());
                    passwordResetRepository.save(reset);

                    // Send notification email (no redirect link — frontend handles navigation)
                    emailService.sendHtmlEmail(
                            user.getEmail(),
                            "Password Reset Request",
                            "reset-password",
                            Map.of(
                                    "userName", user.getEmail(),
                                    "resetToken", plainToken,
                                    "expiresIn", "15 minutes"));

                    log.info("Password reset token generated for user {}", user.getId());

                    return ForgotPasswordResponse.builder()
                            .message("Reset token generated. Use it within 15 minutes.")
                            .resetToken(plainToken)
                            .build();
                })
                // Silent fail: never reveal whether email is registered
                .orElse(ForgotPasswordResponse.builder()
                        .message("If that email is registered, a reset token has been sent.")
                        .resetToken(null)
                        .build());
    }
}
