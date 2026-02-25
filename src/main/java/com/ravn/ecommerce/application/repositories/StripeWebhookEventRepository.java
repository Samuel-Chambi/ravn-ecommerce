package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.payment.StripeWebhookEvent;

import java.util.Optional;

public interface StripeWebhookEventRepository {
    StripeWebhookEvent save(StripeWebhookEvent event);

    Optional<StripeWebhookEvent> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
