package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.StripeWebhookEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StripeWebhookEventJpaRepository extends JpaRepository<StripeWebhookEventJpaEntity, Long> {
    Optional<StripeWebhookEventJpaEntity> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
