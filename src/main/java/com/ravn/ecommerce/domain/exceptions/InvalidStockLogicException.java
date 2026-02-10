package com.ravn.ecommerce.domain.exceptions;

public class InvalidStockLogicException extends RuntimeException {
    public InvalidStockLogicException(String message) {
        super(message);
    }
}
