package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.domain.model.order.RefundStatus;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.RefundJpaEntity;
import org.springframework.stereotype.Component;

@Component
/*
 * RefundStatus mapping:
 * 1 -> PENDING
 * 2 -> APPROVED
 * 3 -> REJECTED
 */
public class RefundMapper implements Mapper<RefundJpaEntity, Refund> {

    @Override
    public Refund toDomain(RefundJpaEntity entity) {
        return Refund.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .userId(entity.getUserId())
                .reason(entity.getReason())
                .adminNote(entity.getAdminNote())
                .status(switch (entity.getStatus()) {
                    case 2 -> RefundStatus.APPROVED;
                    case 3 -> RefundStatus.REJECTED;
                    default -> RefundStatus.PENDING;
                })
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public RefundJpaEntity toJpaEntity(Refund domain) {
        return RefundJpaEntity.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .userId(domain.getUserId())
                .reason(domain.getReason())
                .adminNote(domain.getAdminNote())
                .status(switch (domain.getStatus()) {
                    case APPROVED -> 2;
                    case REJECTED -> 3;
                    default -> 1;
                })
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
