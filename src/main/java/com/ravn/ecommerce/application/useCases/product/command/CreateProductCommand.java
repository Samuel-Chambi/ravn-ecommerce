package com.ravn.ecommerce.application.useCases.product.command;

import com.ravn.ecommerce.application.dto.request.product.ProductRequest;

public record CreateProductCommand(
        ProductRequest productRequest,
        Long userId
) {
}
