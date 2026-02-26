package com.ravn.ecommerce.application.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundRequest {
    @NotNull(message = "OrderId is required")
    private Long orderId;

    @NotBlank(message = "Reason is required")
    private String reason;
}
