package com.ravn.ecommerce.application.dto.response;

import com.ravn.ecommerce.domain.model.product.ProductImage;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {
    @NotNull
    private Long id;
    @NotNull
    private Long productId;
    @NotNull
    private String imageUrl;
    @NotNull
    private Boolean isPrimary;
    @NotNull
    private LocalDateTime createdAt;

    public static ProductImageResponse toDto(ProductImage productImage){
        return ProductImageResponse.builder()
                .id(productImage.getId())
                .productId(productImage.getProductId())
                .imageUrl(productImage.getImageUrl())
                .isPrimary(productImage.isPrimary())
                .createdAt(productImage.getCreatedAt())
                .build();
    }
}
