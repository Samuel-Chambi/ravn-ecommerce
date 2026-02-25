package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.ProductMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public List<Product> findAllById(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return jpaRepository.findById(productId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long productId){
        return jpaRepository.existsById(productId);
    }

    @Override
    @Transactional
    public Product save(Product product) {
        ProductJpaEntity jpaEntity = mapper.toJpaEntity(product);
        ProductJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long productId) {
        // DeleteById -> Soft delete using deletedAt field
         jpaRepository.deleteById(productId);
    }

    @Override
    public Window<Product> findBy(ScrollPosition position, int limit, Sort sort) {
        return jpaRepository.findBy(position, Limit.of(limit), sort)
                .map(mapper::toDomain);
    }

    @Override
    public Window<Product> findByIsEnabled(boolean isEnabled, ScrollPosition position, int limit, Sort sort) {
        return jpaRepository.findByIsEnabled(isEnabled, position, Limit.of(limit), sort)
                .map(mapper::toDomain);
    }

    @Override
    public Window<Product> findByCategoryId(Long categoryId, ScrollPosition position, int limit, Sort sort) {
        return jpaRepository.findByCategoryId(categoryId, position, Limit.of(limit), sort)
                .map(mapper::toDomain);
    }

    @Override
    public Window<Product> findByCategoryIdAndIsEnabled(Long categoryId, boolean isEnabled, ScrollPosition position,
                                                        int limit, Sort sort) {
        return jpaRepository.findByCategoryIdAndIsEnabled(categoryId, isEnabled, position, Limit.of(limit), sort)
                .map(mapper::toDomain);
    }

    @Override
    public Window<Product> searchByName(String name, boolean isEnabled, ScrollPosition position, int limit, Sort sort) {
        return jpaRepository
                .findByNameContainingIgnoreCaseAndIsEnabled(name, isEnabled, position, Limit.of(limit), sort)
                .map(mapper::toDomain);
    }

    @Override
    public Window<Product> searchByNameAndCategory(String name, Long categoryId, boolean isEnabled,
                                                   ScrollPosition position, int limit, Sort sort) {
        return jpaRepository
                .findByNameContainingIgnoreCaseAndCategoryIdAndIsEnabled(name, categoryId, isEnabled, position,
                        Limit.of(limit),
                        sort)
                .map(mapper::toDomain);
    }
}
