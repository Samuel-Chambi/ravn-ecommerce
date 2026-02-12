package com.ravn.ecommerce.domain.exceptions;

public class InvalidOrderException extends DomainException {
    public InvalidOrderException(String message) {
        super(message, "INVALID_ORDER");
    }
}
