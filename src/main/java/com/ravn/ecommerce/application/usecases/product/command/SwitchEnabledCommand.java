package com.ravn.ecommerce.application.usecases.product.command;

public record SwitchEnabledCommand(
        Long userId,
        Long productId
) {
}
