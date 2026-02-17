package com.ravn.ecommerce.domain.exceptions;

public class BusinessRuleViolation extends DomainException {
    public BusinessRuleViolation(String message) {
        super(message, "BUSINESS_RULE_VIOLATION");
    }
}
