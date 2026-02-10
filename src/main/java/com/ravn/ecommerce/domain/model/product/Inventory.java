package com.ravn.ecommerce.domain.model.product;

import com.ravn.ecommerce.domain.exceptions.BussinessRoleViolation;
import com.ravn.ecommerce.domain.exceptions.InsufficientStockException;
import com.ravn.ecommerce.domain.exceptions.InvalidStockLogicException;

import java.time.LocalDateTime;
import java.util.Objects;

public class Inventory {
    private Long id;
    private Long productId;
    private int quantity;
    private LocalDateTime updatedAt;

    public Inventory() {
    }

    public Inventory(Long id, Long productId, int quantity, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }

    // Métodos de negocio
    public void addStock(int amount) {
        if (amount <= 0) {
            throw new InvalidStockLogicException("Amount must be positive");
        }
        this.quantity += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void removeStock(int amount) {
        if (amount <= 0) {
            throw new InvalidStockLogicException("Amount must be positive");
        }
        if (this.quantity < amount) {
            throw new InsufficientStockException(productId.toString() , amount , quantity);
        }
        this.quantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasStock() {
        return this.quantity > 0;
    }

    public boolean hasStock(int amount) {
        return this.quantity >= amount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
        return Objects.equals(id, inventory.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
