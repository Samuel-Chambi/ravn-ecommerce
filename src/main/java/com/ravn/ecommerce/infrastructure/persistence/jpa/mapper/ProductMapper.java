package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.InventoryJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import org.springframework.stereotype.Component;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ProductImageJpaEntity;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper implements Mapper<ProductJpaEntity, Product> {
    private final ProductImageMapper productImageMapper;
    private final InventoryMapper inventoryMapper;

    @Override
    public Product toDomain(ProductJpaEntity entity) {
        if (entity == null)
            return null;

        List<ProductImage> images = (entity.getImages() != null)
                ? entity.getImages().stream()
                .map(productImageMapper::toDomain)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .isEnabled(entity.isEnabled())
                .categoryId(entity.getCategoryId())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .images(images)
                .inventory(inventoryMapper.toDomain(entity.getInventory()))
                .build();
    }

    @Override
    public ProductJpaEntity toJpaEntity(Product product) {
        if (product == null)
            return null;

        List<ProductImageJpaEntity> imageEntities = (product.getImages() != null)
                ? product.getImages().stream()
                .map(productImageMapper::toJpaEntity)
                .collect(Collectors.toList())
                : Collections.emptyList();

        ProductJpaEntity entity = ProductJpaEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .isEnabled(product.isEnabled())
                .categoryId(product.getCategoryId())
                .createdBy(product.getCreatedBy())
                .createdAt(product.getCreatedAt())
                .images(imageEntities)
                .build();

        if (product.getInventory() != null) {
            InventoryJpaEntity inventoryEntity = inventoryMapper.toJpaEntity(product.getInventory());
            inventoryEntity.setProduct(entity);
            entity.setInventory(inventoryEntity);
        }

        return entity;
    }
}
