package com.ravn.ecommerce.domain.model.payment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    private Long id;
    private Long orderId;
    private String stripePaymentIntent;
    private String clientSecret;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void markAsSucceeded() {
        if (this.status == PaymentStatus.SUCCEEDED) {
            return;
        }

        if (this.status == PaymentStatus.FAILED) {
            throw new IllegalStateException("Cannot mark a failed payment as succeeded");
        }

        this.status = PaymentStatus.SUCCEEDED;
    }

    public void markAsFailed() {
        if (this.status == PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Cannot mark a succeeded payment as failed");
        }

        this.status = PaymentStatus.FAILED;
    }

    public void markAsRefunded() {
        if (this.status != PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Only a succeeded payment can be refunded");
        }

        this.status = PaymentStatus.REFUNDED;
    }

    public boolean isSucceeded() {
        return this.status == PaymentStatus.SUCCEEDED;
    }

    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.CREATED;
    }

    public boolean canBeProcessed() {
        return this.status == PaymentStatus.CREATED;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
