package com.ravn.ecommerce.application.usecases.product.command;

import com.ravn.ecommerce.application.dto.request.product.ProductRequest;

public record CreateProductCommand(
        ProductRequest productRequest,
        Long userId
) {
}
