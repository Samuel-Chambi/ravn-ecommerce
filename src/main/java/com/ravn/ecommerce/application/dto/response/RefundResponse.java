package com.ravn.ecommerce.application.dto.response;

import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.domain.model.order.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private String reason;
    private String adminNote;
    private RefundStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RefundResponse toDto(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .orderId(refund.getOrderId())
                .userId(refund.getUserId())
                .reason(refund.getReason())
                .adminNote(refund.getAdminNote())
                .status(refund.getStatus())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .build();
    }
}
