package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.product.Like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    void save(Like like);

    void deleteById(Long likeId);

    Optional<Like> findByUserIdAndProductId(Long userId, Long productId);

    List<Like> findAllByUserId(Long userId);
}
