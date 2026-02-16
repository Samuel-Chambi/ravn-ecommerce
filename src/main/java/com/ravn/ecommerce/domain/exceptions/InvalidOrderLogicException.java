package com.ravn.ecommerce.domain.exceptions;

public class InvalidOrderLogicException extends DomainException {
    public InvalidOrderLogicException(String message) {
        super(message, "INVALID_ORDER_LOGIC");
    }
}
