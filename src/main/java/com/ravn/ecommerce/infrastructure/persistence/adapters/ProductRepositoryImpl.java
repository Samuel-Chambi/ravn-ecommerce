package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.ProductMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implementation of ProductRepository using JPA
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
}
