package com.ravn.ecommerce.application.usecases.category.command;

import com.ravn.ecommerce.application.dto.request.category.CreateCategoryRequest;

public record CreateCategoryCommand(
        CreateCategoryRequest request,
        Long userId
) {
}
