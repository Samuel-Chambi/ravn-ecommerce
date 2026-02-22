package com.ravn.ecommerce.application.useCases.cart.command;

public record RemoveItemFromCartCommand(
        Long userId,
        Long productId
) {
}
