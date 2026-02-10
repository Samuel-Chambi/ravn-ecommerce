package com.ravn.ecommerce.domain.exceptions;

public class CartNotActiveException extends RuntimeException {
    public CartNotActiveException(String message) {
        super(message);
    }
}
