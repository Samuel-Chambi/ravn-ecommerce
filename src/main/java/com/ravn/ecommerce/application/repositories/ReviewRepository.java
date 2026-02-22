package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.product.Review;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Review save(Review review);
    Optional<Review> findById(Long reviewId);
    void deleteById(Long reviewId);
    Window<Review> findAllByProductId(ScrollPosition position , int limit , Long productId);
}
