package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.product.Product;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product domain model.
 * Uses cursor-based pagination (Window API) for better performance.
 */
public interface ProductRepository {
    Optional<Product> findById(Long productId);

    List<Product> findAllById(List<Long> ids);

    Product save(Product product);

    void deleteById(Long productId);

    boolean existsById(Long productId);

    Window<Product> findBy(ScrollPosition position, int limit, Sort sort);

    Window<Product> findByIsEnabled(boolean isEnabled, ScrollPosition position, int limit, Sort sort);

    Window<Product> findByCategoryId(Long categoryId, ScrollPosition position, int limit, Sort sort);

    Window<Product> findByCategoryIdAndIsEnabled(Long categoryId, boolean isEnabled, ScrollPosition position, int limit, Sort sort);

    // Search methods
    Window<Product> searchByName(String name, boolean isEnabled, ScrollPosition position, int limit, Sort sort);

    Window<Product> searchByNameAndCategory(String name, Long categoryId, boolean isEnabled, ScrollPosition position, int limit, Sort sort);

    // Search methods (ignoring status - for managers)
    Window<Product> searchByName(String name, ScrollPosition position, int limit, Sort sort);

    Window<Product> searchByNameAndCategory(String name, Long categoryId, ScrollPosition position, int limit, Sort sort);
}