package com.ravn.ecommerce.auth.infrastructure.persistance.jpa;

import com.ravn.ecommerce.auth.domain.model.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class UserEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Column(nullable = false)
    private boolean active;
}
