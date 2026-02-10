package com.ravn.ecommerce.domain.exceptions;

public class InvalidOrderLogicException extends RuntimeException {
    public InvalidOrderLogicException(String message) {
        super(message);
    }
}
