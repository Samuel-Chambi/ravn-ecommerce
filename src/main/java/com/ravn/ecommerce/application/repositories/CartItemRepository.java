package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.cart.CartItem;

import java.util.Optional;

public interface CartItemRepository {
    CartItem save(CartItem cartItem);
    Optional<CartItem> findByProductIdAndCartId(Long productId, Long cartId);
    boolean existsByProductIdAndCartId(Long productId , Long cartId);
}
