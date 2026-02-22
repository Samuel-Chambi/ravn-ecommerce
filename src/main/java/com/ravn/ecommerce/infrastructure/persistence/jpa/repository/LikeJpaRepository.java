package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.LikeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeJpaRepository extends JpaRepository<LikeJpaEntity, Long> {
    Optional<LikeJpaEntity> findByUserIdAndProductId(Long userId, Long productId);

    List<LikeJpaEntity> findAllByUserId(Long userId);
}
