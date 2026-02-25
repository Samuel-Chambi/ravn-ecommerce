package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.payment.StripeWebhookEvent;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.StripeWebhookEventJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class StripeWebhookEventMapper implements Mapper<StripeWebhookEventJpaEntity , StripeWebhookEvent>{
    @Override
    public StripeWebhookEvent toDomain(StripeWebhookEventJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return StripeWebhookEvent.builder()
                .id(entity.getId())
                .eventId(entity.getEventId())
                .eventType(entity.getEventType())
                .payload(entity.getPayload())
                .processed(entity.isProcessed())
                .receivedAt(entity.getReceivedAt() != null ? entity.getReceivedAt() : LocalDateTime.now())
                .build();
    }
    @Override
    public StripeWebhookEventJpaEntity toJpaEntity(StripeWebhookEvent domain) {
        if (domain == null) {
            return null;
        }
        return StripeWebhookEventJpaEntity.builder()
                .id(domain.getId())
                .eventId(domain.getEventId())
                .eventType(domain.getEventType())
                .payload(domain.getPayload())
                .processed(domain.isProcessed())
                .receivedAt(domain.getReceivedAt() != null ? domain.getReceivedAt() : LocalDateTime.now())
                .build();
    }
}
