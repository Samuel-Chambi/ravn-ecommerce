package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ReviewJpaEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {
    Window<ReviewJpaEntity> findByProductId(Long productId, ScrollPosition position, Limit limit);

}
