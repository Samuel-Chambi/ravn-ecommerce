package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.PasswordResetRepository;
import com.ravn.ecommerce.domain.model.user.PasswordReset;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.PasswordResetJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.PasswordResetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PasswordResetRepositoryImpl implements PasswordResetRepository {

        private final PasswordResetJpaRepository passwordResetJpaRepository;

        @Override
        public PasswordReset save(PasswordReset passwordReset) {
                PasswordResetJpaEntity entity = PasswordResetJpaEntity.builder()
                                .id(passwordReset.getId())
                                .userId(passwordReset.getUserId())
                                .tokenHash(passwordReset.getToken())
                                .expiresAt(passwordReset.getExpiresAt())
                                .used(passwordReset.isUsed())
                                .createdAt(java.time.LocalDateTime.now())
                                .build();
                PasswordResetJpaEntity saved = passwordResetJpaRepository.save(entity);

                return new PasswordReset(saved.getId(), saved.isUsed(), saved.getExpiresAt(),
                                saved.getTokenHash(), saved.getUserId());
        }

        @Override
        public Optional<PasswordReset> findByTokenHash(String tokenHash) {
                return passwordResetJpaRepository.findByTokenHash(tokenHash)
                                .map(e -> new PasswordReset(e.getId(), e.isUsed(), e.getExpiresAt(),
                                                e.getTokenHash(), e.getUserId()));
        }

        @Override
        @Transactional
        public void invalidateAllForUser(Long userId) {
                passwordResetJpaRepository.markAllUsedByUserId(userId);
        }
}
