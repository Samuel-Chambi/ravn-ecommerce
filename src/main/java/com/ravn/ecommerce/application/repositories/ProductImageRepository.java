package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.product.ProductImage;

import java.util.List;
import java.util.Optional;
// ProductImageRepository Interface
public interface ProductImageRepository {
    ProductImage save(ProductImage productImage);

    List<ProductImage> findByProductId(Long productId);

    Optional<ProductImage> findById(Long imageId);

    Optional<ProductImage> findPrimaryByProductId(Long productId);

    void deleteById(Long imageId);

    void deleteAllByProductId(Long productId);

    void unmarkAllAsPrimaryByProductId(Long productId);

    int countByProductId(Long productId);
}
