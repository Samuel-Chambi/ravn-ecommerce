package com.ravn.ecommerce.domain.model.product;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Objects;

@Builder
public class ProductImage {
    private Long id;
    private Long productId;
    private String imageUrl;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
    public static ProductImage create(Long productId , String imageUrl , Boolean isPrimary) {
        return ProductImage.builder()
                .productId(productId)
                .imageUrl(imageUrl)
                .isPrimary(isPrimary != null ? isPrimary : false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void markAsPrimary() {
        this.isPrimary = true;
    }

    public void unmarkAsPrimary() {
        this.isPrimary = false;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt(){ return createdAt;}

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductImage that = (ProductImage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
