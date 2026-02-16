package com.ravn.ecommerce.domain.model.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Payment {
    private Long id;
    private Long orderId;
    private String stripePaymentIntent;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;

    public Payment() {
    }

    public Payment(Long id, Long orderId, String stripePaymentIntent, BigDecimal amount,
                   PaymentStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.stripePaymentIntent = stripePaymentIntent;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStripePaymentIntent() {
        return stripePaymentIntent;
    }

    public void setStripePaymentIntent(String stripePaymentIntent) {
        this.stripePaymentIntent = stripePaymentIntent;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
