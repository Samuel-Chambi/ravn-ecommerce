package com.ravn.ecommerce.application.usecases.cart.command;

public record AddItemToCartCommand(
        Long userId,
        Long productId,
        Integer quantity
) {
}
