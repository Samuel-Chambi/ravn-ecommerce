package com.ravn.ecommerce.domain.exceptions;

public class EmptyCartItemException extends RuntimeException {
    public EmptyCartItemException(String message) {
        super(message);
    }
}
