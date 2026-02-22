package com.ravn.ecommerce.application.usecases.cart.command;

public record RemoveItemFromCartCommand(
        Long userId,
        Long productId
) {
}
