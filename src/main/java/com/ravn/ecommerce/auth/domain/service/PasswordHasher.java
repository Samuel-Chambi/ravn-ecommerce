package com.ravn.ecommerce.auth.domain.service;

import com.ravn.ecommerce.auth.domain.model.PasswordHash;

public interface PasswordHasher {
    PasswordHash hash(String rawPassword);
    boolean matches(String rawPassword, PasswordHash passwordHashed);
}
