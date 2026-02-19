package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.LikeRepository;
import com.ravn.ecommerce.domain.model.product.Like;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.LikeJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.LikeMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.LikeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository jpaRepository;
    private final LikeMapper mapper;

    @Override
    @Transactional
    public void save(Like like) {
        LikeJpaEntity entity = mapper.toJpaEntity(like);
        LikeJpaEntity saved = jpaRepository.save(entity);
        like.setId(saved.getId()); // Update domain ID
    }

    @Override
    @Transactional
    public void deleteById(Long likeId) {
        jpaRepository.deleteById(likeId);
    }

    @Override
    public Optional<Like> findByUserIdAndProductId(Long userId, Long productId) {
        return jpaRepository.findByUserIdAndProductId(userId, productId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Like> findAllByUserId(Long userId) {
        return jpaRepository.findAllByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Like> findAllByProductId(Long productId) {
        return jpaRepository.findAllByProductId(productId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
