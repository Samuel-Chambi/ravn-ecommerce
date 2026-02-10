package com.ravn.ecommerce.domain.exceptions;

public class InvalidCartLogicException extends RuntimeException {
    public InvalidCartLogicException(String message) {
        super(message);
    }
}
