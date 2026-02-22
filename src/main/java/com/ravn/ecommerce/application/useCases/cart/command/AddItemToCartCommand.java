package com.ravn.ecommerce.application.useCases.cart.command;

public record AddItemToCartCommand(
        Long userId,
        Long productId,
        Integer quantity
) {
}
