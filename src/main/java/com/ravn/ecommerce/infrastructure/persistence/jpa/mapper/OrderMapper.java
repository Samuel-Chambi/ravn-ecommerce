package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper implements Mapper<OrderJpaEntity, Order> {
    private final OrderItemMapper orderItemMapper;

    @Override
    public Order toDomain(OrderJpaEntity jpaEntity) {
        return Order.builder()
                .id(jpaEntity.getId())
                .userId(jpaEntity.getUserId())
                .status(
                        switch (jpaEntity.getStatus()) {
                            case 2 -> OrderStatus.PAID;
                            case 3 -> OrderStatus.DELIVERED;
                            case 4 -> OrderStatus.CANCELLED;
                            case 5 -> OrderStatus.SHIPPED;
                            default -> OrderStatus.PENDING;
                        })
                .items(jpaEntity.getOrderItems() != null ? jpaEntity.getOrderItems().stream()
                        .map(orderItemMapper::toDomain)
                        .collect(Collectors.toCollection(ArrayList::new))
                        : null)
                .totalAmount(jpaEntity.getOrderItems() != null ? jpaEntity.getOrderItems().stream()
                        .map(entity -> entity.getPrice().multiply(BigDecimal.valueOf(entity.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        : BigDecimal.ZERO)
                .shippingAddressId(jpaEntity.getShippingAddressId())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }

    @Override
    public OrderJpaEntity toJpaEntity(Order domainEntity) {
        return OrderJpaEntity.builder()
                .id(domainEntity.getId())
                .userId(domainEntity.getUserId())
                .status(switch (domainEntity.getStatus()) {
                    case OrderStatus.PAID -> 2;
                    case OrderStatus.DELIVERED -> 3;
                    case OrderStatus.CANCELLED -> 4;
                    case OrderStatus.SHIPPED -> 5;
                    default -> 1;
                })
                .total(domainEntity.getTotalAmount())
                .shippingAddressId(domainEntity.getShippingAddressId())
                .createdAt(domainEntity.getCreatedAt())
                .updatedAt(domainEntity.getUpdatedAt())
                .orderItems(domainEntity.getItems() != null ? domainEntity.getItems().stream()
                        .map(orderItemMapper::toJpaEntity)
                        .collect(Collectors.toCollection(ArrayList::new)) : null)
                .build();
    }
}
