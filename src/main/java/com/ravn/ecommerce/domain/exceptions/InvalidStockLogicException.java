package com.ravn.ecommerce.domain.exceptions;

public class InvalidStockLogicException extends DomainException {
    public InvalidStockLogicException(String message) {
        super(message, "INVALID_STOCK_LOGIC");
    }
}
