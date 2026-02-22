package com.ravn.ecommerce.application.dto.request.product;

import com.ravn.ecommerce.application.validation.ValidPrice;
import com.ravn.ecommerce.application.validation.ValidStock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 1000, message = "Product name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 100 characters")
    private String description;

    @NotNull(message = "Price is required")
    @ValidPrice(min = 0.01, max = 9999999.90, message = "Price must be between 0.01 and 9999999.90")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @ValidStock(min = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;

    @NotNull(message = "Product must have enabled status")
    private Boolean enabled;
}
