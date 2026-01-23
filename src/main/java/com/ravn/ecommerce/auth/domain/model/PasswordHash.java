package com.ravn.ecommerce.auth.domain.model;

import lombok.Value;

@Value
public class PasswordHash {
    String value;

    public PasswordHash(String value) {
        if (value == null || value.length() < 20) {
            throw new IllegalArgumentException("Invalid password hash");
        }
        this.value = value;
    }
    public static PasswordHash fromHash(String hash){
        return new PasswordHash(hash);
    }
}
