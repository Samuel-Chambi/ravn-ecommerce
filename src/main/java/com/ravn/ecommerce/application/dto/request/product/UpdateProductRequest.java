package com.ravn.ecommerce.application.dto.request.product;

import com.ravn.ecommerce.application.validation.ValidPrice;
import com.ravn.ecommerce.application.validation.ValidStock;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * All fields are OPTIONAL to support partial updates (PATCH semantics).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(min = 3, max = 1000, message = "Product name must be between 3 and 1000 characters")
    private String name;

    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @ValidPrice(min = 0.01, max = 9999999.90, message = "Price must be between 0.01 and 9999999.90")
    private BigDecimal price;

    @ValidStock(min = 0, message = "Stock cannot be negative")
    private Integer stock;

    @Positive(message = "Category ID must be positive")
    private Long categoryId;

    private Boolean enabled;
}
