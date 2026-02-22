package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.user.PasswordReset;

import java.util.Optional;

public interface PasswordResetRepository {
    PasswordReset save(PasswordReset passwordReset);

    Optional<PasswordReset> findByTokenHash(String tokenHash);

    void invalidateAllForUser(Long userId);
}
