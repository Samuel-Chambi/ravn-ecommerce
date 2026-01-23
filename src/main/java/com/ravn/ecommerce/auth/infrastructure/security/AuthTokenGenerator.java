package com.ravn.ecommerce.auth.infrastructure.security;

import com.ravn.ecommerce.auth.domain.model.User;

public interface AuthTokenGenerator {
    String generate(User user);
}
