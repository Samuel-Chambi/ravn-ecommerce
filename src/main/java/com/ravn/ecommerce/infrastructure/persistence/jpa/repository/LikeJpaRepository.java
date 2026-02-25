package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.LikeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;

@Repository
public interface LikeJpaRepository extends JpaRepository<LikeJpaEntity, Long> {
    Optional<LikeJpaEntity> findByUserIdAndProductId(Long userId, Long productId);

    List<LikeJpaEntity> findAllByUserId(Long userId);

    Window<LikeJpaEntity> findByUserId(Long userId, ScrollPosition position, Limit limit);

    List<LikeJpaEntity> findAllByProductId(Long productId);
}
