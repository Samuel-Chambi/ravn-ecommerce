package com.ravn.ecommerce.domain.model.cart;

import com.ravn.ecommerce.domain.exceptions.CartNotActiveException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {
    private Long id;
    private Long userId;
    private CartStatus status;
    private LocalDateTime createdAt;
    private List<CartItem> items;

    public Cart() {
    }

    public Cart(Long id, Long userId, CartStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.items = new ArrayList<>();
    }

    public void addItem(CartItem item) {
        if (this.status != CartStatus.ACTIVE) {
            throw new CartNotActiveException("Cannot add items to a non-active cart");
        }

        if (this.items == null) {
            this.items = new ArrayList<>();
        }

        Optional<CartItem> existingItem = findItemByProductId(item.getProductId());
        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity(item.getQuantity());
        } else {
            this.items.add(item);
        }
    }

    public void removeItem(Long productId) {
        if (this.items == null) return;

        this.items.removeIf(item -> item.getProductId().equals(productId));
    }

    public void updateItemQuantity(Long productId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(productId);
            return;
        }

        Optional<CartItem> item = findItemByProductId(productId);
        item.ifPresent(cartItem -> cartItem.setQuantity(newQuantity));
    }

    public void clear() {
        if (this.items != null) {
            this.items.clear();
        }
    }

    public void markAsOrdered() {
        this.status = CartStatus.ORDERED;
    }

    public boolean isEmpty() {
        return this.items == null || this.items.isEmpty();
    }

    public int getTotalItems() {
        if (this.items == null) return 0;

        return this.items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public BigDecimal getTotalAmount() {
        if (this.items == null) return BigDecimal.ZERO;

        return this.items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Optional<CartItem> findItemByProductId(Long productId) {
        if (this.items == null) return Optional.empty();

        return this.items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }
}
