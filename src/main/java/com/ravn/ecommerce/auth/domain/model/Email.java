package com.ravn.ecommerce.auth.domain.model;

import lombok.Value;

@Value
public class Email {
    String value;

    public Email(String value) {
        if (value == null || !value.matches("^[^@]+@[^@]+\\\\.[^@]+$")) {
            throw new IllegalArgumentException("Invalid email.");
        }
        this.value = value;
    }
    public static Email from(String value){
        return new Email(value);
    }
}
