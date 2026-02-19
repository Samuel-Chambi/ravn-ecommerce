package com.ravn.ecommerce.application.usecases.product.command;

public record DeleteProductCommand(
        Long productId,
        Long userId
) {
}
