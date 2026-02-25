package com.ravn.ecommerce.domain.exceptions;

public class UserNotFound extends EntityNotFoundException {
    public UserNotFound(String message) {
        super(message, "USER_NOT_FOUND");
    }
}
