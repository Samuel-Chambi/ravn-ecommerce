package com.ravn.ecommerce.domain.model.order;

import com.ravn.ecommerce.domain.exceptions.InvalidOrderLogicException;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long shippingAddressId;
    private List<OrderItem> items;

    public void markAsPaid() {
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidOrderLogicException("Only pending orders can be marked as paid");
        }
        this.status = OrderStatus.PAID;
    }

    public void markAsShipped() {
        if (this.status != OrderStatus.PAID) {
            throw new InvalidOrderLogicException("Only paid orders can be shipped");
        }
        this.status = OrderStatus.SHIPPED;
    }

    public void markAsDelivered() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new InvalidOrderLogicException("Only shipped orders can be marked as delivered");
        }
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (this.status == OrderStatus.SHIPPED) {
            throw new InvalidOrderLogicException("Cannot cancel shipped orders");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public boolean canBeCancelled() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.PAID;
    }

    public boolean isPending() {
        return this.status == OrderStatus.PENDING;
    }

    public boolean isPaid() {
        return this.status == OrderStatus.PAID;
    }

    public boolean isShipped() {
        return this.status == OrderStatus.SHIPPED;
    }

    public boolean isCancelled() {
        return this.status == OrderStatus.CANCELLED;
    }

    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    public BigDecimal calculateTotal() {
        if (this.items == null || this.items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return this.items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        if (this.items == null)
            return 0;
        return this.items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
