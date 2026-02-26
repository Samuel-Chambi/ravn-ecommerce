package com.ravn.ecommerce.presentation.rest.controllers;

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
public class RefundController {

    private final RequestRefundUseCase requestRefundUseCase;
    private final ApproveRefundUseCase approveRefundUseCase;
    private final RejectRefundUseCase rejectRefundUseCase;
    private final GetUserRefundsUseCase getUserRefundsUseCase;
    private final GetPendingRefundsUseCase getPendingRefundsUseCase;
    private final CurrentUserService currentUserService;

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

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RefundResponse>> getMyRefunds() {
        Long userId = currentUserService.getCurrentUserId();
        List<RefundResponse> refunds = getUserRefundsUseCase.execute(userId);
        return ResponseEntity.status(HttpStatus.OK).body(refunds);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<RefundResponse>> getPendingRefunds() {
        List<RefundResponse> refunds = getPendingRefundsUseCase.execute(null);
        return ResponseEntity.status(HttpStatus.OK).body(refunds);
    }

    @PostMapping("/{refundId}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> approveRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody ReviewRefund dto) {

        approveRefundUseCase.execute(new ApproveRefundCommand(refundId, dto.getAdminNote()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{refundId}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> rejectRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody ReviewRefund dto) {

        rejectRefundUseCase.execute(new RejectRefundCommand(refundId, dto.getAdminNote()));
        return ResponseEntity.ok().build();
    }
}
