package com.ravn.ecommerce.application.usecases.refund.command;

public record ApproveRefundCommand(Long refundRequestId, String adminNote) {
}
