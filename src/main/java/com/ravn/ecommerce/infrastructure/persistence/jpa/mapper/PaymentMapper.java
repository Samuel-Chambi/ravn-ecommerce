package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.payment.Payment;
import com.ravn.ecommerce.domain.model.payment.PaymentStatus;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.PaymentJpaEntity;
import org.springframework.stereotype.Component;

@Component
/*
 * Payment status mapping:
 * 1 -> CREATED
 * 2 -> SUCCEEDED
 * 3 -> FAILED
 * 4 -> REFUNDED
 */
public class PaymentMapper implements Mapper<PaymentJpaEntity, Payment>{

    @Override
    public Payment toDomain(PaymentJpaEntity entity) {
        return Payment.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .clientSecret(entity.getClientSecret())
                .stripePaymentIntent(entity.getStripePaymentIntent())
                .amount(entity.getAmount())
                .status(switch (entity.getStatus()) {
                    case 2 -> PaymentStatus.SUCCEEDED;
                    case 3 -> PaymentStatus.FAILED;
                    case 4 -> PaymentStatus.REFUNDED;
                    default -> PaymentStatus.CREATED;
                })
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public PaymentJpaEntity toJpaEntity(Payment domain) {
        return PaymentJpaEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .stripePaymentIntent(domain.getStripePaymentIntent())
                .clientSecret(domain.getClientSecret())
                .amount(domain.getAmount())
                .status(switch (domain.getStatus()) {
                    case SUCCEEDED -> 2;
                    case FAILED -> 3;
                    case REFUNDED -> 4;
                    default -> 1;
                })
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
