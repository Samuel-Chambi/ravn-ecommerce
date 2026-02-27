package com.ravn.ecommerce.presentation.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ravn.ecommerce.application.dto.response.RefundResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.usecases.refund.command.ApproveRefundCommand;
import com.ravn.ecommerce.application.usecases.refund.ApproveRefundUseCase;
import com.ravn.ecommerce.application.usecases.refund.GetPendingRefundsUseCase;
import com.ravn.ecommerce.application.usecases.refund.GetUserRefundsUseCase;
import com.ravn.ecommerce.application.usecases.refund.command.RejectRefundCommand;
import com.ravn.ecommerce.application.usecases.refund.RejectRefundUseCase;
import com.ravn.ecommerce.application.usecases.refund.command.RequestRefundCommand;
import com.ravn.ecommerce.application.usecases.refund.RequestRefundUseCase;
import com.ravn.ecommerce.application.dto.request.order.RefundRequest;
import com.ravn.ecommerce.application.dto.request.order.ReviewRefund;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/refunds")
@RequiredArgsConstructor
@Tag(name = "Refunds", description = "Endpoints for requesting, viewing, and processing order refunds")
public class RefundController {

    private final RequestRefundUseCase requestRefundUseCase;
    private final ApproveRefundUseCase approveRefundUseCase;
    private final RejectRefundUseCase rejectRefundUseCase;
    private final GetUserRefundsUseCase getUserRefundsUseCase;
    private final GetPendingRefundsUseCase getPendingRefundsUseCase;
    private final CurrentUserService currentUserService;

    @Operation(summary = "Request a refund", description = "Allows a user to request a refund for a specific completed order. Provides a reason for the request.")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RefundResponse> requestRefund(
            @Valid @RequestBody RefundRequest requestDto) {
        Long userId = currentUserService.getCurrentUserId();
        RequestRefundCommand command = new RequestRefundCommand(userId, requestDto.getOrderId(),
                requestDto.getReason());
        RefundResponse response = requestRefundUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get my refunds", description = "Retrieves a list of all refund requests made by the currently authenticated user.")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RefundResponse>> getMyRefunds() {
        Long userId = currentUserService.getCurrentUserId();
        List<RefundResponse> refunds = getUserRefundsUseCase.execute(userId);
        return ResponseEntity.status(HttpStatus.OK).body(refunds);
    }

    @Operation(summary = "Get pending refunds (Admin)", description = "Retrieves a list of all unresolved (pending) refund requests in the system. Requires MANAGER role.")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<RefundResponse>> getPendingRefunds() {
        List<RefundResponse> refunds = getPendingRefundsUseCase.execute(null);
        return ResponseEntity.status(HttpStatus.OK).body(refunds);
    }

    @Operation(summary = "Approve a refund (Admin)", description = "Approves a pending refund request and processes the payment refund. Requires MANAGER role.")
    @PostMapping("/{refundId}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> approveRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody ReviewRefund dto) {

        approveRefundUseCase.execute(new ApproveRefundCommand(refundId, dto.getAdminNote()));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reject a refund (Admin)", description = "Rejects a pending refund request with a provided reason. Requires MANAGER role.")
    @PostMapping("/{refundId}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> rejectRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody ReviewRefund dto) {

        rejectRefundUseCase.execute(new RejectRefundCommand(refundId, dto.getAdminNote()));
        return ResponseEntity.ok().build();
    }
}
