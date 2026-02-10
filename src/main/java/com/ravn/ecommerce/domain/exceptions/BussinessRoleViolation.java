package com.ravn.ecommerce.domain.exceptions;

public class BussinessRoleViolation extends RuntimeException {
    public BussinessRoleViolation(String message) {
        super(message);
    }
}
