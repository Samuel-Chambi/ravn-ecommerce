package com.ravn.ecommerce.application.usecases.refund.command;

public record RequestRefundCommand(Long userId, Long orderId, String reason) {
}
