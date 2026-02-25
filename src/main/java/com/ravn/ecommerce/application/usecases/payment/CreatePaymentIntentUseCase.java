package com.ravn.ecommerce.application.usecases.payment;

import com.ravn.ecommerce.application.dto.response.PaymentResponse;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.PaymentRepository;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.payment.Payment;
import com.ravn.ecommerce.domain.model.payment.PaymentStatus;
import com.ravn.ecommerce.infrastructure.stripe.StripeService;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreatePaymentIntentUseCase {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;

    @Transactional
    public PaymentResponse execute(Long orderId) {
        log.info("Creating payment intent for order ID {}", orderId);

        // Idempotency: if a payment already exists for this order, return it
        if (paymentRepository.existsByOrderId(orderId)) {
            return paymentRepository.findByOrderId(orderId)
                    .map(PaymentResponse::toDto)
                    .orElseThrow();
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        try {
            PaymentIntent intent = stripeService.createPaymentIntent(order.getTotalAmount(), "usd");

            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setStripePaymentIntent(intent.getId());
            payment.setClientSecret(intent.getClientSecret());
            payment.setAmount(order.getTotalAmount());
            payment.setStatus(PaymentStatus.CREATED);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());

            Payment saved = paymentRepository.save(payment);
            log.info("Payment intent {} created for order {}", intent.getId(), orderId);
            return PaymentResponse.toDto(saved);

        } catch (Exception e) {
            log.error("Failed to create Stripe PaymentIntent for order {}: {}", orderId, e.getMessage());
            throw new BusinessRuleViolation("Failed to create payment intent: " + e.getMessage());
        }
    }
}
