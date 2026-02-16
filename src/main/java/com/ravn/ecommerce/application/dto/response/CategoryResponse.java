package com.ravn.ecommerce.application.dto.response;

import com.ravn.ecommerce.domain.model.product.Category;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    @Size(min = 3, max = 1000, message = "Product name must be between 3 and 100 characters")
    private String name;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static CategoryResponse toDto(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .isActive(category.isActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
