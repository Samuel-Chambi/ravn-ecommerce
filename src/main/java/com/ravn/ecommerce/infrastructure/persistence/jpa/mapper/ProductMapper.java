package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.InventoryJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ProductImageJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper implements Mapper<ProductJpaEntity , Product>{
    private final ProductImageMapper productImageMapper;
    private final InventoryMapper inventoryMapper;
    @Override
    public Product toDomain(ProductJpaEntity entity) {
        if (entity == null) return null;
        List<ProductImage> images = (entity.getImages() != null)
                ? entity.getImages().stream()
                .map(productImageMapper::toDomain)
                .toList()
                : Collections.emptyList();

        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .isEnabled(entity.isEnabled())
                .categoryId(entity.getCategoryId())
                .images(images)
                .inventory(inventoryMapper.toDomain(entity.getInventory()))
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    @Override
    public ProductJpaEntity toJpaEntity(Product product) {
        if (product == null) return null;
        List<ProductImageJpaEntity> imageEntities = (product.getImages() != null)
                ? product.getImages().stream()
                .map(productImageMapper::toJpaEntity)
                .toList()
                : Collections.emptyList();
        ProductJpaEntity jpaEntity = ProductJpaEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .isEnabled(product.isEnabled())
                .categoryId(product.getCategoryId())
                .images(imageEntities)
                .createdBy(product.getCreatedBy())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
        if (product.getInventory() != null) {
            InventoryJpaEntity inventoryEntity = inventoryMapper.toJpaEntity(product.getInventory());
            inventoryEntity.setProduct(jpaEntity);
            jpaEntity.setInventory(inventoryEntity);
        }
        return jpaEntity;
    }
}
