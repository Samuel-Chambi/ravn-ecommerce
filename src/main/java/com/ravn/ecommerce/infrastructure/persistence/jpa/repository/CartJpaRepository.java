package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CartJpaRepository extends JpaRepository<CartJpaEntity , Long> {
    Optional<CartJpaEntity> findByStatusAndUserId(int status, Long userId);
}
