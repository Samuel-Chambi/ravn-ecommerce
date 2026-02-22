package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.order.OrderItem;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.OrderItemJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemMapper implements Mapper<OrderItemJpaEntity , OrderItem>{
    @Override
    public OrderItem toDomain(OrderItemJpaEntity jpaEntity) {
        return OrderItem.builder()
                .id(jpaEntity.getId())
                .orderId(jpaEntity.getOrderId())
                .productId(jpaEntity.getProductId())
                .quantity(jpaEntity.getQuantity())
                .price(jpaEntity.getPrice())
                .productName(jpaEntity.getProductName())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }

    @Override
    public OrderItemJpaEntity toJpaEntity(OrderItem domainEntity) {
        return OrderItemJpaEntity.builder()
                .id(domainEntity.getId())
                .orderId(domainEntity.getOrderId())
                .productId(domainEntity.getProductId())
                .productName(domainEntity.getProductName())
                .quantity(domainEntity.getQuantity())
                .price(domainEntity.getPrice())
                .createdAt(domainEntity.getCreatedAt())
                .updatedAt(domainEntity.getUpdatedAt())
                .build();
    }
}
