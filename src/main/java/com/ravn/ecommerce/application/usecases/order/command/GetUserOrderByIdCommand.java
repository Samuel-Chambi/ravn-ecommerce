package com.ravn.ecommerce.application.useCases.order.command;

public record GetUserOrderByIdCommand(
        Long userId,
        Long orderId
) {
}
