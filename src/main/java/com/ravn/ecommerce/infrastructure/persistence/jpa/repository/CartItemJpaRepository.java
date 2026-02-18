package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.CartItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemJpaRepository extends JpaRepository<CartItemJpaEntity , Long> {
    Optional<CartItemJpaEntity> findByProductIdAndCartId(Long productId, Long cartId);
}
