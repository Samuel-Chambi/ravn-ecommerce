package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.CartItemRepository;
import com.ravn.ecommerce.domain.model.cart.CartItem;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.CartItemJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.CartItemMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.CartItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CartItemRepositoryImpl implements CartItemRepository {
    private final CartItemJpaRepository cartItemJpaRepository;
    private final CartItemMapper cartItemMapper;
    @Override
    public CartItem save(CartItem cartItem){
        CartItemJpaEntity entity = cartItemMapper.toJpaEntity(cartItem);
        CartItemJpaEntity saved = cartItemJpaRepository.save(entity);
        return cartItemMapper.toDomain(saved);
    }
    @Override
    public Optional<CartItem> findByProductIdAndCartId(Long productId, Long cartId){
        return cartItemJpaRepository.findByProductIdAndCartId(productId, cartId).map(cartItemMapper::toDomain);
    }
}
