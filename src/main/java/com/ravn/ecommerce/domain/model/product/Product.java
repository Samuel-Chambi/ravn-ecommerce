package com.ravn.ecommerce.domain.model.product;

import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import lombok.*;

import java.lang.Long;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean isEnabled;
    private Long categoryId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductImage> images;
    private Inventory inventory;


    public void enable() {
        this.isEnabled = true;
    }

    public void disable() {
        this.isEnabled = false;
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolation("Price must be greater than zero");
        }
        this.price = newPrice;
    }

    public void addImage(ProductImage image) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(image);
    }

    public ProductImage getPrimaryImage() {
        if (this.images == null || this.images.isEmpty())
            return null;

        return this.images.stream()
                .filter(ProductImage::isPrimary)
                .findFirst()
                .orElse(this.images.getFirst());
    }

    public boolean isAvailable() {
        return isEnabled && hasStock();
    }

    public boolean hasStock() {
        return inventory != null && inventory.hasStock();
    }

    public int getAvailableQuantity() {
        return inventory != null ? inventory.getQuantity() : 0;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
