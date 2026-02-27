package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.RefundJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, Long> {
    List<RefundJpaEntity> findByUserId(Long userId);

    List<RefundJpaEntity> findByStatus(int status);

    boolean existsByOrderIdAndStatusIn(Long orderId, List<Integer> statuses);
}
