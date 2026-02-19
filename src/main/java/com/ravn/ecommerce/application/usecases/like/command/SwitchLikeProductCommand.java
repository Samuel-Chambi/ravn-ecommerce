package com.ravn.ecommerce.application.usecases.like.command;

public record SwitchLikeProductCommand(
        Long productId,
        Long userId
) {
}
