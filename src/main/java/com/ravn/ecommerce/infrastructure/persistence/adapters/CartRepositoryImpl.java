package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartStatus;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.CartJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.CartMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.CartJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {
    private final CartJpaRepository cartJpaRepository;
    private final CartMapper cartMapper;
    @Override
    public Cart save(Cart cart){
        CartJpaEntity entity = cartMapper.toJpaEntity(cart);
        CartJpaEntity saved = cartJpaRepository.save(entity);
        return cartMapper.toDomain(saved);
    }

    @Override
    public Optional<Cart> findByStatusAndUserId(CartStatus status , Long userId){
        return cartJpaRepository.findByStatusAndUserId(
                status == CartStatus.ACTIVE ? 1 : 2,
                userId
        ).map(cartMapper::toDomain);
    }


}
