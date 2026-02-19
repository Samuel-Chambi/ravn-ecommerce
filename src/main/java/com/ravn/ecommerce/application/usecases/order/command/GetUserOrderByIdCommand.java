package com.ravn.ecommerce.application.usecases.order.command;

public record GetUserOrderByIdCommand(
        Long userId,
        Long orderId
) {
}
