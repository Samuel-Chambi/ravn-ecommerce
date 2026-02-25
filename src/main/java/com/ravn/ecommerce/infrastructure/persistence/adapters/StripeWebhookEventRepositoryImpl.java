package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.StripeWebhookEventRepository;
import com.ravn.ecommerce.domain.model.payment.StripeWebhookEvent;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.StripeWebhookEventJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.StripeWebhookEventMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.StripeWebhookEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StripeWebhookEventRepositoryImpl implements StripeWebhookEventRepository {

    private final StripeWebhookEventJpaRepository jpaRepository;
    private final StripeWebhookEventMapper stripeWebhookEventMapper;
    @Override
    public StripeWebhookEvent save(StripeWebhookEvent event) {
        StripeWebhookEventJpaEntity entity = stripeWebhookEventMapper.toJpaEntity(event);
        StripeWebhookEventJpaEntity saved = jpaRepository.save(entity);
        return stripeWebhookEventMapper.toDomain(saved);
    }

    @Override
    public Optional<StripeWebhookEvent> findByEventId(String eventId) {
        return jpaRepository.findByEventId(eventId)
                .map(stripeWebhookEventMapper::toDomain);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaRepository.existsByEventId(eventId);
    }
}
