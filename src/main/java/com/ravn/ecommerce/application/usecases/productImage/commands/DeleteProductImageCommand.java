package com.ravn.ecommerce.application.usecases.productImage.commands;

public record DeleteProductImageCommand(
        Long productId,
        Long imageId
) {
}
