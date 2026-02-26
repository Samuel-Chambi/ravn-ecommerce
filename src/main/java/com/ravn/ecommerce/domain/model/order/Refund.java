package com.ravn.ecommerce.domain.model.order;

import com.ravn.ecommerce.domain.exceptions.InvalidOrderLogicException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Refund {
    private Long id;
    private Long orderId;
    private Long userId;
    private String reason;
    private String adminNote;
    private RefundStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Refund createPending(Long orderId, Long userId, String reason) {
        return Refund.builder()
                .orderId(orderId)
                .userId(userId)
                .reason(reason)
                .status(RefundStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void approve(String note) {
        if (this.status != RefundStatus.PENDING) {
            throw new InvalidOrderLogicException("Only pending refund requests can be approved.");
        }
        this.status = RefundStatus.APPROVED;
        this.adminNote = note;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String note) {
        if (this.status != RefundStatus.PENDING) {
            throw new InvalidOrderLogicException("Only pending refund requests can be rejected.");
        }
        this.status = RefundStatus.REJECTED;
        this.adminNote = note;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == RefundStatus.PENDING;
    }

    public boolean isApproved() {
        return this.status == RefundStatus.APPROVED;
    }

    public boolean isRejected() {
        return this.status == RefundStatus.REJECTED;
    }
}
