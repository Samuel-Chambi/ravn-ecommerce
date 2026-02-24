package com.ravn.ecommerce.application.usecases.order.command;

import com.ravn.ecommerce.domain.model.order.OrderStatus;

public record UpdateDeliveryStatusCommand(
        Long orderId,
        Long managerId,
        OrderStatus newStatus) {
}
