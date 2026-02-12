package com.ravn.ecommerce.domain.exceptions;

public class EmptyCartItemException extends DomainException {
    public EmptyCartItemException(String message) {
        super(message, "EMPTY_CART_ITEM");
    }
}
