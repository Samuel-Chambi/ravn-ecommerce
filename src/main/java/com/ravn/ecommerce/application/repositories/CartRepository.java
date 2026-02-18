package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartStatus;

import java.util.Optional;

public interface CartRepository {
    Cart save(Cart cart);
    Optional<Cart> findByStatusAndUserId(CartStatus status , Long userId);
}
