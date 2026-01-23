package com.ravn.ecommerce.auth.infrastructure.persistance.mapper;

import com.ravn.ecommerce.auth.domain.model.*;
import com.ravn.ecommerce.auth.infrastructure.persistance.jpa.UserEntity;

import java.time.Instant;

public class UserMapper {
    public static UserEntity toEntity(User user){
        UserEntity entity = new UserEntity();
        entity.setId(user.getUserId().value());
        entity.setRole(user.getUserRole());
        entity.setEmail(user.getEmail().getValue());
        entity.setPasswordHash(user.getPassword().getValue());
        entity.setActive(user.isActive());
        return entity;
    }

    public static User toDomain(UserEntity userEntity){
        return new User(
                UserId.from(userEntity.getId().toString()),
                Email.from(userEntity.getEmail()),
                PasswordHash.fromHash(userEntity.getPasswordHash()),
                userEntity.getRole(),
                (userEntity.isActive()) ? UserStatus.ACTIVE : UserStatus.INACTIVE,
                Instant.now()
        );
    }
}
