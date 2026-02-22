package com.ravn.ecommerce.application.dto.response;

import com.ravn.ecommerce.domain.model.product.Inventory;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isEnabled;
    private Long categoryId;
    private List<ProductImage> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Inventory inventory;

    public static ProductResponse toDto(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .isEnabled(product.isEnabled())
                .categoryId(product.getCategoryId())
                .images(product.getImages())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .inventory(product.getInventory())
                .build();
    }
}
