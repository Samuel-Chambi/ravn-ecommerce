package com.ravn.ecommerce.domain.model.order.events;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RefundRejectedEvent {
    private final Long refundId;
    private final Long orderId;
    private final Long userId;
    private final String adminNote;
    private final LocalDateTime occurredOn;

    public RefundRejectedEvent(Long refundId, Long orderId, Long userId, String adminNote) {
        this.refundId = refundId;
        this.orderId = orderId;
        this.userId = userId;
        this.adminNote = adminNote;
        this.occurredOn = LocalDateTime.now();
    }
}
