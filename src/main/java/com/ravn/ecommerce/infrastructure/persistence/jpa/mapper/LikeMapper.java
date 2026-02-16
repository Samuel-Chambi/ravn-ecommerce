package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.product.Like;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.LikeJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class LikeMapper {

    public Like toDomain(LikeJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Like.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .productId(entity.getProductId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public LikeJpaEntity toJpaEntity(Like domain) {
        if (domain == null) {
            return null;
        }
        return LikeJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .productId(domain.getProductId())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
