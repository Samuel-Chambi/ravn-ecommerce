package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.product.Product;

import java.util.Optional;
// ProductRepository Interface
public interface ProductRepository {
    Optional<Product> findById(Long id);
}
