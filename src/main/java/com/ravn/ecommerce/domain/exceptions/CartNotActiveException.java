package com.ravn.ecommerce.domain.exceptions;

public class CartNotActiveException extends DomainException {
    public CartNotActiveException(String message) {
        super(message, "CART_NOT_ACTIVE");
    }
}
