package com.ravn.ecommerce.application.dto.request.category;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    @Size(min = 3, max = 1000, message = "Category name must be between 3 and 100 characters")
    private String name;
}
