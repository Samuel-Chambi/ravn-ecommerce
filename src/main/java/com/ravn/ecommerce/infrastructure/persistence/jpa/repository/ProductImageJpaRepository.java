package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ProductImageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageJpaRepository extends JpaRepository<ProductImageJpaEntity , Long> {
    List<ProductImageJpaEntity> findByProductId(Long productId);

    Optional<ProductImageJpaEntity> findByProductIdAndIsPrimaryTrue(Long productId);

    int countByProductId(Long productId);

    void deleteAllByProductId(Long productId);

    @Modifying
    @Query("UPDATE ProductImageJpaEntity pi SET pi.isPrimary = false WHERE pi.productId = :productId")
    void unmarkAllAsPrimaryForProduct(@Param("productId") Long productId);
}
