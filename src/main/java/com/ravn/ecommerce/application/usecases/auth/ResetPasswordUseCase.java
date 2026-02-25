package com.ravn.ecommerce.application.usecases.auth;

import com.ravn.ecommerce.application.dto.request.auth.ResetPasswordRequest;
import com.ravn.ecommerce.application.repositories.PasswordResetRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.services.EmailService;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.user.PasswordReset;
import com.ravn.ecommerce.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResetPasswordUseCase implements UseCase<ResetPasswordRequest, String> {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional
    public String execute(ResetPasswordRequest request) {
        log.info("Reset password attempt");

        PasswordReset reset = passwordResetRepository.findByTokenHash(request.getToken())
                .orElseThrow(() -> new EntityNotFoundException("Invalid or expired token"));

        if (!reset.isValid()) {
            throw new EntityNotFoundException("Token has expired or already been used");
        }

        User user = userRepository.findById(reset.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        reset.markAsUsed();
        passwordResetRepository.save(reset);

        // Send confirmation email
        emailService.sendHtmlEmail(
                user.getEmail(),
                "Password Changed Successfully",
                "password-changed",
                Map.of("userName", user.getEmail()));

        log.info("Password reset successfully for user {}", user.getId());
        return "Password updated successfully";
    }
}
