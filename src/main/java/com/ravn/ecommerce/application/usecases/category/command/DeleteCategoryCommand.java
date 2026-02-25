package com.ravn.ecommerce.application.usecases.category.command;

public record DeleteCategoryCommand(
        Long categoryId,
        Long userId
) {
}
