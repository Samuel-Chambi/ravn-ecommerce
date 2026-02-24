package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartStatus;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.CartItemJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.CartJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartMapper implements Mapper<CartJpaEntity, Cart> {
        private final CartItemMapper cartItemMapper;

        @Override
        public Cart toDomain(CartJpaEntity cartJpaEntity) {
                return Cart.builder()
                                .id(cartJpaEntity.getId())
                                .userId(cartJpaEntity.getUserId())
                                .status(cartJpaEntity.getStatus() == 1 ? CartStatus.ACTIVE : CartStatus.ORDERED)
                                .total(cartJpaEntity.getCartItems() != null ? cartJpaEntity.getCartItems().stream()
                                                .map((jpaEntity) -> {
                                                        return jpaEntity.getPrice() != null ?
                                                                jpaEntity.getPrice().multiply(BigDecimal.valueOf(jpaEntity.getQuantity()))
                                                                : BigDecimal.ZERO;
                                                })
                                                .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO)
                                .items(cartJpaEntity.getCartItems() != null ? cartJpaEntity.getCartItems().stream()
                                                .map(cartItemMapper::toDomain)
                                                .collect(Collectors.toCollection(ArrayList::new))
                                                : null)
                                .createdAt(cartJpaEntity.getCreatedAt())
                                .updatedAt(cartJpaEntity.getUpdatedAt())
                                .build();
        }

        @Override
        public CartJpaEntity toJpaEntity(Cart cart) {
                return CartJpaEntity.builder()
                                .id(cart.getId())
                                .userId(cart.getUserId())
                                .status(cart.getStatus() == CartStatus.ACTIVE ? 1 : 2)
                                .cartItems(cart.getItems() != null ? cart.getItems().stream()
                                                .map(cartItemMapper::toJpaEntity)
                                                .toList() : null)
                                .createdAt(cart.getCreatedAt())
                                .updatedAt(cart.getUpdatedAt())
                                .build();
        }
}
