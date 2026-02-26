package com.ravn.ecommerce.application.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewRefund {
    @NotBlank(message = "Admin note is required")
    private String adminNote;
}
