package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.user.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long userId);

    User save(User user);

    boolean existsById(Long userId);

    void deleteById(Long userId);
}
