package com.ravn.ecommerce.domain.model.product.events;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ProductDiscountedEvent {
    private final Long productId;
    private final String productName;
    private final BigDecimal oldPrice;
    private final BigDecimal newPrice;
    private final LocalDateTime occurredOn;

    public ProductDiscountedEvent(Long productId, String productName, BigDecimal oldPrice, BigDecimal newPrice) {
        this.productId = productId;
        this.productName = productName;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.occurredOn = LocalDateTime.now();
    }
}
