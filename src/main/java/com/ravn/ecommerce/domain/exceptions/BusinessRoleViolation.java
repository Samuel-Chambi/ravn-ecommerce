package com.ravn.ecommerce.domain.exceptions;

public class BusinessRoleViolation extends DomainException {
    public BusinessRoleViolation(String message) {
        super(message, "BUSINESS_RULE_VIOLATION");
    }
}
