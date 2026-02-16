package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toDomain(ProductJpaEntity entity) {
        if (entity == null) return null;
        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .isEnabled(entity.isEnabled())
                .categoryId(entity.getCategoryId())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ProductJpaEntity toJpaEntity(Product product) {
        if (product == null) return null;
        return ProductJpaEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .isEnabled(product.isEnabled())
                .categoryId(product.getCategoryId())
                .createdBy(product.getCreatedBy())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
