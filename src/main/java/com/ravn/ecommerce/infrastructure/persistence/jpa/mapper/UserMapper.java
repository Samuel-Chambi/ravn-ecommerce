package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper implements Mapper<UserJpaEntity , User>{
    /*
        Roles mapping:
            1 => CLIENT
            2 => MANAGER
    * */
    private final AddressMapper addressMapper;
    @Override
    public User toDomain(UserJpaEntity userJpaEntity) {
        return User.builder()
                .id(userJpaEntity.getId())
                .email(userJpaEntity.getEmail())
                .passwordHash(userJpaEntity.getPasswordHash())
                .role(userJpaEntity.getRole() == 2 ? UserRole.MANAGER : UserRole.CLIENT)
                .createdAt(userJpaEntity.getCreatedAt())
                .updatedAt(userJpaEntity.getUpdatedAt())
                .active(userJpaEntity.getIsActive())
                .addresses(userJpaEntity.getAddresses() != null ?
                        userJpaEntity.getAddresses().stream()
                                .map(addressMapper::toDomain)
                                .collect(Collectors.toCollection(ArrayList::new))
                        : null)
                .build();
    }
    @Override
    public UserJpaEntity toJpaEntity(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole() == UserRole.CLIENT ? 1 : 2)
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .addresses(user.getAddresses() != null ?
                        user.getAddresses().stream()
                                .map(addressMapper::toJpaEntity)
                                .collect(Collectors.toCollection(ArrayList::new))
                        : null)
                .build();
    }
}
