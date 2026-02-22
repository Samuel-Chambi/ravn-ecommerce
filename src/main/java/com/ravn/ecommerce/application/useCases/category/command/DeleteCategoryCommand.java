package com.ravn.ecommerce.application.useCases.category.command;

public record DeleteCategoryCommand(
        Long categoryId,
        Long userId
) {
}
