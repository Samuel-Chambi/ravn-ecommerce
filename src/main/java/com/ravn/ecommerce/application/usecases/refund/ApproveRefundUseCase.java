package com.ravn.ecommerce.application.usecases.refund;

import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.usecases.refund.command.ApproveRefundCommand;
import com.ravn.ecommerce.domain.model.order.events.OrderStatusChangedEvent;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.domain.model.payment.Payment;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.PaymentRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.RefundRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.infrastructure.stripe.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApproveRefundUseCase implements UseCase<ApproveRefundCommand, Void> {

        private final RefundRepository refundRepository;
        private final OrderRepository orderRepository;
        private final PaymentRepository paymentRepository;
        private final ProductRepository productRepository;
        private final StripeService stripeService;
        private final UserRepository userRepository;
        private final EventPublisher eventPublisher;

        @Override
        @Transactional
        public Void execute(ApproveRefundCommand command) {
                log.info("Manager is approving refund request ID {} with note: {}", command.refundRequestId(),
                                command.adminNote());

                Refund refund = refundRepository.findById(command.refundRequestId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                String.format("RefundRequest ID %d not found",
                                                                command.refundRequestId())));

                Order order = orderRepository.findById(refund.getOrderId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                String.format("Order ID %d associated with RefundRequest not found",
                                                                refund.getOrderId())));

                Payment payment = paymentRepository.findByOrderId(order.getId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                String.format("No payment found for Order ID %d", order.getId())));

                // 1. Trigger Stripe Refund
                try {
                        stripeService.refundPayment(payment.getStripePaymentIntent());
                        log.info("Successfully refunded Stripe payment {} for Order ID {}",
                                        payment.getStripePaymentIntent(),
                                        order.getId());
                } catch (Exception e) {
                        log.error("Failed to refund Stripe payment for Order {}", order.getId(), e);
                        throw new RuntimeException("Failed to process refund with Stripe. Refund was aborted.", e);
                }

                // 2. Mark Domain Entities
                payment.markAsRefunded();
                paymentRepository.save(payment);

                refund.approve(command.adminNote());
                refundRepository.save(refund);

                order.markAsRefunded();
                order.setUpdatedAt(LocalDateTime.now());

                // 3. Restock Inventory
                order.getItems().forEach(orderItem -> {
                        productRepository.findById(orderItem.getProductId()).ifPresent(product -> {
                                if (product.getInventory() != null) {
                                        product.getInventory().addStock(orderItem.getQuantity());
                                        productRepository.save(product);
                                        log.info("Restocked {} units of Product {} (ID: {})",
                                                        orderItem.getQuantity(), product.getName(), product.getId());
                                }
                        });
                });

                orderRepository.save(order);

                // 4. Publish Event (e.g., to send an email to the user)
                userRepository.findById(order.getUserId()).ifPresent(user -> {
                        eventPublisher.publish(new OrderStatusChangedEvent(
                                        order.getId(), user.getId(), user.getEmail(), order.getStatus()));
                });

                log.info("Refund request ID {} approved successfully. Order {} is now REFUNDED.", refund.getId(),
                                order.getId());
                return null;
        }
}
