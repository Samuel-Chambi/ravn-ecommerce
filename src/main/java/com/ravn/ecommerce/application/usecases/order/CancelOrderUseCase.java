package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.repositories.PaymentRepository;
import com.ravn.ecommerce.infrastructure.stripe.StripeService;
import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.InvalidOrderException;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.events.OrderStatusChangedEvent;
import com.ravn.ecommerce.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CancelOrderUseCase implements UseCase<Long, Void> {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public Void execute(Long orderId) {
        log.info("Admin cancelling order ID {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Order ID %d does not exist", orderId)));

        if (!order.canBeCancelled()) {
            throw new InvalidOrderException(
                    String.format("Order ID %d cannot be cancelled (current status: %s)", orderId, order.getStatus()));
        }

        if (order.isPaid()) {
            paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
                try {
                    stripeService.refundPayment(payment.getStripePaymentIntent());
                    payment.markAsRefunded();
                    paymentRepository.save(payment);
                    log.info("Payment ID {} successfully refunded via Stripe", payment.getId());
                } catch (Exception e) {
                    log.error("Failed to refund payment for order {}", orderId, e);
                    throw new RuntimeException("Failed to process refund. Order was not cancelled.", e);
                }
            });
        }

        order.cancel();
        order.setUpdatedAt(LocalDateTime.now());

        order.getItems().forEach(orderItem -> {
            productRepository.findById(orderItem.getProductId()).ifPresent(product -> {
                if (product.getInventory() != null) {
                    product.getInventory().addStock(orderItem.getQuantity());
                    productRepository.save(product);
                }
            });
        });

        orderRepository.save(order);

        // Notify user
        userRepository.findById(order.getUserId()).map(User::getEmail)
                .ifPresent(email -> eventPublisher.publish(new OrderStatusChangedEvent(
                        order.getId(), order.getUserId(), email, order.getStatus())));

        log.info("Order ID {} cancelled by admin", orderId);
        return null;
    }
}
