package com.ravn.ecommerce.application.useCases.productImage.commands;

public record DeleteProductImageCommand(
        Long productId,
        Long imageId
) {
}
