package com.ravn.ecommerce.application.dto.request;

import com.ravn.ecommerce.application.validation.ValidOrderStatus;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryStatusRequest {

    @NotBlank(message = "Status is required")
    @ValidOrderStatus(
            allowedStatuses = {OrderStatus.PENDING, OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.CANCELLED},
            message = "Invalid delivery status"
    )
    private String status;
}