package com.ravn.ecommerce.domain.model.cart;

import com.ravn.ecommerce.domain.exceptions.CartNotActiveException;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Cart {
    private Long id;
    private Long userId;
    private CartStatus status;
    private BigDecimal total;
    private List<CartItem> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public void addItem(CartItem item) {
        if (this.status != CartStatus.ACTIVE) {
            throw new CartNotActiveException("Cannot add items to a non-active cart");
        }

        if (this.items == null) {
            this.items = new ArrayList<>();
        }

        Optional<CartItem> existingItem = findItemByProductId(item.getProductId());
        if (existingItem.isPresent()) {
            total = total.add(existingItem.get().getSubTotal().multiply(BigDecimal.valueOf(-1)));
            existingItem.get().increaseQuantity(item.getQuantity());
            total = total.add(existingItem.get().getSubTotal());
        } else {
            this.items.add(item);
            total = total.add(item.getSubTotal());
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

    public Optional<CartItem> findItemByProductId(Long productId) {
        if (this.items == null) return Optional.empty();

        return this.items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }
}
