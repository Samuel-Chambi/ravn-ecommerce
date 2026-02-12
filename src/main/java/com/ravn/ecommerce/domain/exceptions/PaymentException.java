package com.ravn.ecommerce.domain.exceptions;

public class PaymentException extends DomainException {
    public PaymentException(String message) {
        super(message, "PAYMENT_ERROR");
    }
}
