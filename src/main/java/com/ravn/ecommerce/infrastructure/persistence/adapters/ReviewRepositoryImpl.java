package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.ReviewRepository;
import com.ravn.ecommerce.domain.model.product.Review;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.ReviewMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {
    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public Review save(Review review) {
        var entity = reviewMapper.toJpaEntity(review);
        var savedEntity = reviewJpaRepository.save(entity);
        return reviewMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        return reviewJpaRepository.findById(reviewId)
                .map(reviewMapper::toDomain);
    }

    @Override
    public Window<Review> findAllByProductId(ScrollPosition position, int limit, Long productId) {
        return reviewJpaRepository.findByProductId(productId, position, Limit.of(limit)).map(reviewMapper::toDomain);
    }

    @Override
    public void deleteById(Long reviewId) {
        reviewJpaRepository.deleteById(reviewId);
    }
}
