package com.ravn.ecommerce.domain.model.order.events;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RefundRequestedEvent {
    private final Long refundId;
    private final Long orderId;
    private final Long userId;
    private final String reason;
    private final LocalDateTime occurredOn;

    public RefundRequestedEvent(Long refundId, Long orderId, Long userId, String reason) {
        this.refundId = refundId;
        this.orderId = orderId;
        this.userId = userId;
        this.reason = reason;
        this.occurredOn = LocalDateTime.now();
    }
}
