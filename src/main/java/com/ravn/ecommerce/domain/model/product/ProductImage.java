package com.ravn.ecommerce.domain.model.product;

import java.util.Objects;

public class ProductImage {
    private Long id;
    private Long productId;
    private String imageUrl;
    private boolean isPrimary;

    public ProductImage() {
    }

    public ProductImage(Long id, Long productId, String imageUrl, boolean isPrimary) {
        this.id = id;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.isPrimary = isPrimary;
    }


    public void setAsPrimary() {
        this.isPrimary = true;
    }

    public void unsetPrimary() {
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
