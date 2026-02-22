package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.product.Review;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.ReviewJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper implements Mapper<ReviewJpaEntity , Review> {
    @Override
    public Review toDomain(ReviewJpaEntity jpaEntity){
        return Review.builder()
                .id(jpaEntity.getId())
                .userId(jpaEntity.getUserId())
                .productId(jpaEntity.getProductId())
                .rating(jpaEntity.getRating())
                .comment(jpaEntity.getComment())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }

    @Override
    public ReviewJpaEntity toJpaEntity(Review review){
        return ReviewJpaEntity.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .productId(review.getProductId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
