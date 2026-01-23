package com.ravn.ecommerce.auth.infrastructure.security;

import com.ravn.ecommerce.auth.domain.model.PasswordHash;
import com.ravn.ecommerce.auth.domain.service.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    public BCryptPasswordHasher() {}

    @Override
    public PasswordHash hash(String rawPassword){
        return new PasswordHash(encoder.encode(rawPassword));
    }
    @Override
    public boolean matches(String rawPassword, PasswordHash passwordHash){
        return encoder.matches(rawPassword , passwordHash.getValue());
    }
}
