package com.ravn.ecommerce.application.useCases.product.command;

public record DeleteProductCommand(
        Long productId,
        Long userId
) {
}
