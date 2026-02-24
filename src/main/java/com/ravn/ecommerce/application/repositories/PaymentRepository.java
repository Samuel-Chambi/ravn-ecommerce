package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.payment.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByStripePaymentIntent(String stripePaymentIntent);

    boolean existsByOrderId(Long orderId);
}
