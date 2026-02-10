package com.ravn.ecommerce.domain.exceptions;

public class InvalidOrderException extends RuntimeException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
