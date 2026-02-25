package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.product.Like;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;

public interface LikeRepository {
    void save(Like like);

    void deleteById(Long likeId);

    Optional<Like> findByUserIdAndProductId(Long userId, Long productId);

    List<Like> findAllByUserId(Long userId);

    Window<Like> findAllByUserId(Long userId, ScrollPosition position, Limit limit);

    List<Like> findAllByProductId(Long productId);
}
