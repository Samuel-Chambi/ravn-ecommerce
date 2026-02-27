package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
    @EntityGraph(attributePaths = "orderItems")
    Optional<OrderJpaEntity> findById(Long id);

    @EntityGraph(attributePaths = "orderItems")
    List<OrderJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "orderItems")
    Optional<OrderJpaEntity> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = "orderItems")
    Window<OrderJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, ScrollPosition position, Limit limit);

    @EntityGraph(attributePaths = "orderItems")
    Window<OrderJpaEntity> findAllByOrderByCreatedAtDesc(ScrollPosition position, Limit limit);
}
