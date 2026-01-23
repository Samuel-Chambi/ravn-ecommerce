package com.ravn.ecommerce.auth.domain.repository;

import com.ravn.ecommerce.auth.domain.model.Email;
import com.ravn.ecommerce.auth.domain.model.User;
import com.ravn.ecommerce.auth.domain.model.UserId;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(Email email);
    Optional<User> findById(UserId userId);

    void save(User user);
}
