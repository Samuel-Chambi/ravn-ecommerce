package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.cart.CartItem;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.CartItemJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper implements Mapper<CartItemJpaEntity , CartItem>{
    @Override
    public CartItem toDomain(CartItemJpaEntity cartItemJpaEntity){
        return CartItem.builder()
                .id(cartItemJpaEntity.getId())
                .cartId(cartItemJpaEntity.getCartId())
                .productId(cartItemJpaEntity.getProductId())
                .quantity(cartItemJpaEntity.getQuantity())
                .subTotal(cartItemJpaEntity.getSubTotal())
                .createdAt(cartItemJpaEntity.getCreatedAt())
                .updatedAt(cartItemJpaEntity.getUpdatedAt())
                .build();
    }
    @Override
    public CartItemJpaEntity toJpaEntity(CartItem cartItem){
        return CartItemJpaEntity.builder()
                .id(cartItem.getId())
                .cartId(cartItem.getCartId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .subTotal(cartItem.getSubTotal())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}
