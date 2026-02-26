package com.ravn.ecommerce.domain.model.order;

/*
* Mapping:
* 1 -> PENDING
* 2 -> PAID
* 3 -> DELIVERED
* 4 -> CANCELLED
* 5 -> SHIPPED
* 6 -> REFUNDED
* */
public enum OrderStatus {
    PENDING,
    PAID,
    DELIVERED,
    CANCELLED,
    SHIPPED,
    REFUNDED
}
