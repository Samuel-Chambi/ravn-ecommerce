package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {
    Window<ProductJpaEntity> findBy(ScrollPosition position, Limit limit, Sort sort);

    Window<ProductJpaEntity> findByIsEnabled(boolean isEnabled, ScrollPosition position, Limit limit, Sort sort);

    Window<ProductJpaEntity> findByCategoryId(Long categoryId, ScrollPosition position, Limit limit, Sort sort);

    Window<ProductJpaEntity> findByCategoryIdAndIsEnabled(Long categoryId, boolean isEnabled, ScrollPosition position,
                                                          Limit limit, Sort sort);

    Window<ProductJpaEntity> findByNameContainingIgnoreCaseAndIsEnabled(String name, boolean isEnabled,
                                                                        ScrollPosition position, Limit limit, Sort sort);

    Window<ProductJpaEntity> findByNameContainingIgnoreCaseAndCategoryIdAndIsEnabled(String name, Long categoryId,
                                                                                     boolean isEnabled, ScrollPosition position, Limit limit, Sort sort);

    // Searching ignore status :: NOTE -> Maybe is only for MANAGER role
    Window<ProductJpaEntity> findByNameContainingIgnoreCase(String name, ScrollPosition position, Limit limit,
                                                            Sort sort);

    Window<ProductJpaEntity> findByNameContainingIgnoreCaseAndCategoryId(String name, Long categoryId,
                                                                         ScrollPosition position, Limit limit, Sort sort);
}
