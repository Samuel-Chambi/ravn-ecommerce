package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    /*
        Roles mapping:
            1 => CLIENT
            2 => MANAGER
    * */
    public User toDomain(UserJpaEntity userJpaEntity) {
        return User.builder()
                .id(userJpaEntity.getId())
                .email(userJpaEntity.getEmail())
                .passwordHash(userJpaEntity.getPasswordHash())
                .role(userJpaEntity.getRole() == 2 ? UserRole.MANAGER : UserRole.CLIENT)
                .createdAt(userJpaEntity.getCreatedAt())
                .active(userJpaEntity.getIsActive())
                .build();
    }

    public UserJpaEntity toJpaEntity(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole() == UserRole.CLIENT ? 1 : 2)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
