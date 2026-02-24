package com.ravn.ecommerce.domain.exceptions;

public class ProductNotFound extends EntityNotFoundException {
    public ProductNotFound(String message) {
        super(message, "PRODUCT_NOT_FOUND");
    }
}
