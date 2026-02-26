package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long userId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);

    boolean existsById(Long userId);

    void deleteById(Long userId);

    List<User> findByRole(UserRole role);
}
