package com.ravn.ecommerce.domain.exceptions;

public class ProductNotFound extends DomainException {
    public ProductNotFound(String message) {
        super(message, "PRODUCT_NOT_FOUND");
    }
}
