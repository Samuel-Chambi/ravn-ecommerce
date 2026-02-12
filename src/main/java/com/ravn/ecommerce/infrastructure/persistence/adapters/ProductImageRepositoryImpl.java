package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.ProductImageRepository;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ProductImageJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.ProductImageMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.ProductImageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductImageRepositoryImpl implements ProductImageRepository {
    private final ProductImageJpaRepository jpaRepository;
    private final ProductImageMapper mapper;

    @Override
    public ProductImage save(ProductImage productImage) {
        ProductImageJpaEntity entity = mapper.toJpaEntity(productImage);
        ProductImageJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ProductImage> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ProductImage> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ProductImage> findPrimaryByProductId(Long productId) {
        return jpaRepository.findByProductIdAndIsPrimaryTrue(productId)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long imageId) {
        jpaRepository.deleteById(imageId);
    }

    @Override
    public void deleteAllByProductId(Long productId) {
        jpaRepository.deleteAllByProductId(productId);
    }

    @Override
    public void unmarkAllAsPrimaryByProductId(Long productId) {
        jpaRepository.unmarkAllAsPrimaryForProduct(productId);
    }

    @Override
    public int countByProductId(Long productId) {
        return jpaRepository.countByProductId(productId);
    }
}
