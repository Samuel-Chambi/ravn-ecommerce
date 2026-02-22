package com.ravn.ecommerce.domain.model.order.events;

import com.ravn.ecommerce.domain.model.order.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderStatusChangedEvent {
    private final Long orderId;
    private final Long userId;
    private final String userEmail;
    private final OrderStatus newStatus;
    private final LocalDateTime occurredOn;

    public OrderStatusChangedEvent(Long orderId, Long userId, String userEmail, OrderStatus newStatus) {
        this.orderId = orderId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.newStatus = newStatus;
        this.occurredOn = LocalDateTime.now();
    }
}
