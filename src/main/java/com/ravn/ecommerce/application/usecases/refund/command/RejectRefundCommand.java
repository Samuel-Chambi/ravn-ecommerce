package com.ravn.ecommerce.application.usecases.refund.command;

public record RejectRefundCommand(Long refundRequestId, String adminNote) {
}
