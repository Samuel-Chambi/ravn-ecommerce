package com.ravn.ecommerce.application.usecases.payment;

import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.PaymentRepository;
import com.ravn.ecommerce.application.repositories.StripeWebhookEventRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import com.ravn.ecommerce.domain.model.order.events.OrderStatusChangedEvent;
import com.ravn.ecommerce.domain.model.payment.Payment;
import com.ravn.ecommerce.domain.model.payment.StripeWebhookEvent;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.infrastructure.stripe.StripeService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class HandleStripeWebhookUseCase {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;
    private final StripeService stripeService;
    private final StripeWebhookEventRepository stripeWebhookEventRepository;

    @Transactional
    public void execute(String payload, String sigHeader) {
        Event event;
        try {
            event = stripeService.constructWebhookEvent(payload, sigHeader);
        } catch (Exception e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid Stripe webhook signature");
        }

        log.info("Processing Stripe webhook event: {} ({})", event.getType(), event.getId());

        // Idempotency check: don't process if we already did
        if (stripeWebhookEventRepository.existsByEventId(event.getId())) {
            log.info("Stripe Webhook Event {} was already processed. Skipping.", event.getId());
            return;
        }

        // Save raw event for auditing
        StripeWebhookEvent dbEvent = StripeWebhookEvent.builder()
                .eventId(event.getId())
                .eventType(event.getType())
                .payload(payload) // raw JSON string from request
                .processed(false)
                .receivedAt(LocalDateTime.now())
                .build();
        dbEvent = stripeWebhookEventRepository.save(dbEvent);

        com.stripe.model.EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;

        if (deserializer.getObject().isPresent()) {
            stripeObject = deserializer.getObject().get();
        } else {
            // API version mismatch between event and SDK — use raw deserialization
            log.warn("API version mismatch for event {} — using raw deserializer", event.getId());
            try {
                stripeObject = deserializer.deserializeUnsafe();
            } catch (Exception e) {
                log.error("Failed to deserialize Stripe event {}: {}", event.getId(), e.getMessage());
                return;
            }
        }

        boolean successfullyProcessed = true;
        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = (PaymentIntent) stripeObject;
                handlePaymentSucceeded(intent);
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent intent = (PaymentIntent) stripeObject;
                handlePaymentFailed(intent);
            }
            default -> {
                log.debug("Unhandled Stripe event type: {}", event.getType());
                // For unhandled types we also mark as processed so we don't try again
            }
        }

        // Mark as processed safely regardless of handler
        dbEvent.markAsProcessed();
        stripeWebhookEventRepository.save(dbEvent);
    }

    private void handlePaymentSucceeded(PaymentIntent intent) {
        paymentRepository.findByStripePaymentIntent(intent.getId()).ifPresentOrElse(payment -> {
            if (payment.isSucceeded()) {
                log.info("Payment {} already marked as succeeded (idempotent)", intent.getId());
                return;
            }
            payment.markAsSucceeded();
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Update order status to CONFIRMED
            orderRepository.findById(payment.getOrderId()).ifPresent(order -> {
                order.markAsPaid();
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                notifyOrderStatusChange(order);
                log.info("Order {} marked as paid after successful payment", order.getId());
            });
        }, () -> log.warn("No payment found for PaymentIntent {}", intent.getId()));
    }

    private void handlePaymentFailed(PaymentIntent intent) {
        paymentRepository.findByStripePaymentIntent(intent.getId()).ifPresentOrElse(payment -> {
            if (payment.isFailed()) {
                log.info("Payment {} already marked as failed (idempotent)", intent.getId());
                return;
            }
            payment.markAsFailed();
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Order stays in PENDING — client can retry
            log.warn("Payment intent {} failed for order {}", intent.getId(), payment.getOrderId());
        }, () -> log.warn("No payment found for PaymentIntent {}", intent.getId()));
    }

    private void notifyOrderStatusChange(Order order) {
        userRepository.findById(order.getUserId())
                .map(User::getEmail)
                .ifPresent(email -> eventPublisher.publish(
                        new OrderStatusChangedEvent(order.getId(), order.getUserId(), email, order.getStatus())));
    }
}
