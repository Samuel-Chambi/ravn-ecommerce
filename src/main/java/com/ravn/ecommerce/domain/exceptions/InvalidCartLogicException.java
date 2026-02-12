package com.ravn.ecommerce.domain.exceptions;

public class InvalidCartLogicException extends DomainException {
    public InvalidCartLogicException(String message) {
        super(message, "INVALID_CART_LOGIC");
    }
}
