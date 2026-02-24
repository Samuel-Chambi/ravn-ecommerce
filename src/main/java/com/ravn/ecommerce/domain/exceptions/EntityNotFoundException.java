package com.ravn.ecommerce.domain.exceptions;

public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String entityName, Object ID) {
        super(
                String.format("%s with ID '%s' not found", entityName, ID),
                "ENTITY_NOT_FOUND");
    }

    public EntityNotFoundException(String message) {
        super(message, "ENTITY_NOT_FOUND");
    }

    protected EntityNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}
