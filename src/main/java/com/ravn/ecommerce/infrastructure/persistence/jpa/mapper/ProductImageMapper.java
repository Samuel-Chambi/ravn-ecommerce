package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.product.ProductImage;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ProductImageJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductImageMapper implements Mapper<ProductImageJpaEntity, ProductImage> {

    @Override
    public ProductImage toDomain(ProductImageJpaEntity productImageJpaEntity) {
        if (productImageJpaEntity == null)
            return null;
        return ProductImage.builder()
                .id(productImageJpaEntity.getId())
                .productId(productImageJpaEntity.getProductId())
                .imageUrl(productImageJpaEntity.getImageUrl())
                .isPrimary(productImageJpaEntity.getIsPrimary())
                .createdAt(productImageJpaEntity.getCreatedAt())
                .build();
    }

    @Override
    public ProductImageJpaEntity toJpaEntity(ProductImage productImage) {
        if (productImage == null)
            return null;
        return ProductImageJpaEntity.builder()
                .id(productImage.getId())
                .productId(productImage.getProductId())
                .imageUrl(productImage.getImageUrl())
                .isPrimary(productImage.isPrimary())
                .createdAt(productImage.getCreatedAt())
                .build();
    }
}
