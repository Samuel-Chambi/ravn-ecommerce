package com.ravn.ecommerce.auth.domain.service;

import com.ravn.ecommerce.auth.domain.model.PasswordHash;

public interface PasswordPolicy {
    void validate(PasswordHash passwordHash);
}
