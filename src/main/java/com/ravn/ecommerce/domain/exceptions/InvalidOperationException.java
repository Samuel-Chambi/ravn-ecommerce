package com.ravn.ecommerce.domain.exceptions;

public class InvalidOperationException extends DomainException {
    public InvalidOperationException(String message , String errorCode) {
        super(message, errorCode);
    }
    public InvalidOperationException(String message){
        super(message, "INVALID_OPERATION");
    }
}
