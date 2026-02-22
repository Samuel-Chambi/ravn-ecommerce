package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.product.Category;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category domain model.
 */
public interface CategoryRepository {
    Optional<Category> findById(Long id);

    Window<Category> findBy(ScrollPosition position, int limit);

    Category save(Category category);

    void deleteById(Long categoryId);
}
