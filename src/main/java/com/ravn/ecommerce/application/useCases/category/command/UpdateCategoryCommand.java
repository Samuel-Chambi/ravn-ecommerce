package com.ravn.ecommerce.application.useCases.category.command;

import com.ravn.ecommerce.application.dto.request.category.UpdateCategoryRequest;

public record UpdateCategoryCommand(
        UpdateCategoryRequest request,
        Long userId,
        Long categoryId
) {
}
