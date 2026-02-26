package com.ravn.ecommerce.domain.exceptions;

import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends DomainException {

    public TooManyRequestsException() {
        super("Too many requests. Please wait before trying again.", "TOO_MANY_REQUESTS_EXCEPTION");
    }

    public TooManyRequestsException(String message) {
        super(message, "TOO_MANY_REQUESTS_EXCEPTION");
    }

    public HttpStatus getStatus() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }
}
