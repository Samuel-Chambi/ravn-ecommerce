package com.ravn.ecommerce.domain.model.cart;

import com.ravn.ecommerce.domain.exceptions.EmptyCartItemException;
import com.ravn.ecommerce.domain.exceptions.InvalidCartLogicException;

import java.math.BigDecimal;
import java.util.Objects;

public class CartItem {
    private Long id;
    private Long cartId;
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;
    private String productName;

    public CartItem() {
    }

    public CartItem(Long id, Long cartId, Long productId, int quantity, BigDecimal unitPrice, String productName) {
        this.id = id;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.productName = productName;
    }

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

    public BigDecimal getSubtotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
