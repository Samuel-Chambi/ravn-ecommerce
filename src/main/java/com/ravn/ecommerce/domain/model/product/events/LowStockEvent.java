package com.ravn.ecommerce.domain.model.product.events;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LowStockEvent {
    private final Long productId;
    private final String productName;
    private final int currentStock;
    private final LocalDateTime occurredOn;

    public LowStockEvent(Long productId, String productName, int currentStock) {
        this.productId = productId;
        this.productName = productName;
        this.currentStock = currentStock;
        this.occurredOn = LocalDateTime.now();
    }
}
