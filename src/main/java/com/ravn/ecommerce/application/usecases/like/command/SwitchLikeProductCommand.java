package com.ravn.ecommerce.application.useCases.like.command;

public record SwitchLikeProductCommand(
        Long productId,
        Long userId
) {
}
