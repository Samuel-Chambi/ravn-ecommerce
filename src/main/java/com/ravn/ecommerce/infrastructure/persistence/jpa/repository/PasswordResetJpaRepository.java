package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.PasswordResetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface PasswordResetJpaRepository extends JpaRepository<PasswordResetJpaEntity, Long> {
    Optional<PasswordResetJpaEntity> findByTokenHash(String tokenHash);
    @Modifying
    @Query("UPDATE PasswordResetJpaEntity p SET p.used = true WHERE p.userId = :userId AND p.used = false")
    void markAllUsedByUserId(Long userId);
}
