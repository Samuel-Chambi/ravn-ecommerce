package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.UserMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;
    private final UserMapper mapper;

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = mapper.toJpaEntity(user);
        UserJpaEntity saved = userJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsById(Long userId) {
        return userJpaRepository.existsById(userId);
    }

    @Override
    public void deleteById(Long userId) {
        userJpaRepository.deleteById(userId);
    }

    @Override
    public List<User> findByRole(UserRole role) {
        int roleInt = role == UserRole.MANAGER ? 2 : 1;
        return userJpaRepository.findByRole(roleInt).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findAllById(List<Long> ids) {
        return userJpaRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
