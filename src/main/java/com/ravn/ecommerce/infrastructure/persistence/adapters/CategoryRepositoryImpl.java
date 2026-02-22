package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.domain.model.product.Category;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.CategoryMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {
    private final CategoryMapper categoryMapper;
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Optional<Category> findById(Long categoryId) {
        return categoryJpaRepository.findById(categoryId)
                .map(categoryMapper::toDomain);
    }

    @Override
    public Window<Category> findBy(ScrollPosition position, int limit) {
        return categoryJpaRepository.findBy(position, Limit.of(limit))
                .map(categoryMapper::toDomain);
    }

    @Override
    public Category save(Category category) {
        var entity = categoryMapper.toJpaEntity(category);
        var savedEntity = categoryJpaRepository.save(entity);
        return categoryMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(Long categoryId) {
        categoryJpaRepository.findById(categoryId).ifPresent(entity -> {
            entity.setIsActive(false);
            categoryJpaRepository.save(entity);
        });
    }
}
