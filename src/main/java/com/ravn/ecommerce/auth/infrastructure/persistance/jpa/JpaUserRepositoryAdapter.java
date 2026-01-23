package com.ravn.ecommerce.auth.infrastructure.persistance.jpa;

import com.ravn.ecommerce.auth.domain.model.Email;
import com.ravn.ecommerce.auth.domain.model.User;
import com.ravn.ecommerce.auth.domain.model.UserId;
import com.ravn.ecommerce.auth.domain.repository.UserRepository;
import com.ravn.ecommerce.auth.infrastructure.persistance.mapper.UserMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Transactional
public class JpaUserRepositoryAdapter implements UserRepository {
    private final SpringDataUserRepository repository;
    public JpaUserRepositoryAdapter(SpringDataUserRepository repository){
        this.repository = repository;
    }
    @Override
    public void save(User user){
        repository.save(UserMapper.toEntity(user));
    }

    @Override
    public Optional<User> findByEmail(Email email){
        return repository
                .findByEmail(email.getValue())
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UserId userId){
        return repository
                .findById(userId.value())
                .map(UserMapper::toDomain);
    }
}
