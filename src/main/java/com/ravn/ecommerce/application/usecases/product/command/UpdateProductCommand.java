package com.ravn.ecommerce.application.usecases.product.command;

import com.ravn.ecommerce.application.dto.request.product.UpdateProductRequest;

public record UpdateProductCommand(
        Long productId,
        UpdateProductRequest request,
        Long userId
) {
}
