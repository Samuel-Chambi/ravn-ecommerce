package com.ravn.ecommerce.domain.model.product;

import com.ravn.ecommerce.domain.exceptions.InsufficientStockException;
import com.ravn.ecommerce.domain.exceptions.InvalidStockLogicException;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class Inventory {
    private Long id;
    private Long productId;
    private int quantity;
    private LocalDateTime updatedAt;

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
            throw new InsufficientStockException(productId.toString(), amount, quantity);
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
