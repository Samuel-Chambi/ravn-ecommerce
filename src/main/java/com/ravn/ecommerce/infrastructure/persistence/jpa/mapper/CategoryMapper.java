package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.product.Category;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper implements Mapper<CategoryJpaEntity, Category> {
    @Override
    public Category toDomain(CategoryJpaEntity categoryJpaEntity) {
        return Category.builder()
                .id(categoryJpaEntity.getId())
                .name(categoryJpaEntity.getName())
                .isActive(categoryJpaEntity.getIsActive())
                .createdAt(categoryJpaEntity.getCreatedAt())
                .updatedAt(categoryJpaEntity.getUpdatedAt())
                .build();
    }

    @Override
    public CategoryJpaEntity toJpaEntity(Category category) {
        return CategoryJpaEntity.builder()
                .id(category.getId())
                .name(category.getName())
                .isActive(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
