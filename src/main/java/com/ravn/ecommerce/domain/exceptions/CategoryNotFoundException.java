package com.ravn.ecommerce.domain.exceptions;

/**
 * Exception thrown when a category is not found.
 */
public class CategoryNotFoundException extends DomainException {

    public CategoryNotFoundException(Long categoryId) {
        super(
                String.format("Category not found with id: %d", categoryId),
                "CATEGORY_NOT_FOUND");
    }

    public CategoryNotFoundException(String message) {
        super(message, "CATEGORY_NOT_FOUND");
    }
}
