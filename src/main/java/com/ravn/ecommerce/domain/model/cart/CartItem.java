package com.ravn.ecommerce.domain.model.cart;

import com.ravn.ecommerce.domain.exceptions.EmptyCartItemException;
import com.ravn.ecommerce.domain.exceptions.InvalidCartLogicException;
import lombok.*;

import java.math.BigDecimal;
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
    private int quantity;
    private BigDecimal subTotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Métodos de negocio
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
        BigDecimal unitPrice = subTotal.divide(BigDecimal.valueOf(quantity), java.math.RoundingMode.HALF_UP);
        this.quantity += amount;
        this.subTotal = this.subTotal.add(unitPrice.multiply(BigDecimal.valueOf(amount)));
    }

    public void increaseQuantity(int amount, BigDecimal unitPrice) {
        if (amount <= 0) {
            throw new InvalidCartLogicException("Amount must be positive");
        }
        if (this.subTotal == null) {
            this.subTotal = BigDecimal.ZERO;
        }
        this.quantity += amount;
        this.subTotal = this.subTotal.add(unitPrice.multiply(BigDecimal.valueOf(amount)));
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
