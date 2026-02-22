package com.ravn.ecommerce.application.useCases.product.command;

public record SwitchEnabledCommand(
        Long userId,
        Long productId
) {
}
