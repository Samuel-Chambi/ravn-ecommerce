package com.ravn.ecommerce.domain.model.cart;

import com.ravn.ecommerce.domain.exceptions.EmptyCartItemException;
import com.ravn.ecommerce.domain.exceptions.InvalidCartLogicException;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CartItem {
    private Long id;
    private Long cartId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Business methods
    public void setQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new EmptyCartItemException("Quantity must be positive");
        }
        this.quantity = newQuantity;
    }

    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new InvalidCartLogicException("Amount must be positive");
        }
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new InvalidCartLogicException("Amount must be positive");
        }

        int newQuantity = this.quantity - amount;
        if (newQuantity < 0) {
            throw new InvalidCartLogicException("Resulting quantity cannot be negative");
        }
        this.quantity = newQuantity;
    }

    // Business methods
    public BigDecimal getSubtotal(){
        if(this.price == null) return BigDecimal.ZERO;
        return this.price.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
