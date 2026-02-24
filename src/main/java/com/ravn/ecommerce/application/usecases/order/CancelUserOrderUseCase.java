package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.repositories.PaymentRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.infrastructure.stripe.StripeService;
import com.ravn.ecommerce.application.usecases.order.command.GetUserOrderByIdCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.InvalidOrderException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
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
public class CancelUserOrderUseCase implements UseCase<GetUserOrderByIdCommand, Void> {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public Void execute(GetUserOrderByIdCommand command) {
        log.info("User ID {} cancelling order ID {}", command.userId(), command.orderId());

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFound(
                        String.format("User ID %d does not exist", command.userId())));

        Order order = orderRepository.findByIdAndUserId(command.orderId(), command.userId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Order ID %d not found for user ID %d", command.orderId(),
                                command.userId())));

        if (!order.canBeCancelled()) {
            throw new InvalidOrderException(
                    String.format("Order ID %d cannot be cancelled (current status: %s)",
                            command.orderId(),
                            order.getStatus()));
        }

        if (order.isPaid()) {
            paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
                try {
                    stripeService.refundPayment(payment.getStripePaymentIntent());
                    payment.markAsRefunded();
                    paymentRepository.save(payment);
                    log.info("Payment ID {} successfully refunded via Stripe", payment.getId());
                } catch (Exception e) {
                    log.error("Failed to refund payment for order {}", command.orderId(), e);
                    throw new RuntimeException("Failed to process refund. Order was not cancelled.",
                            e);
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

        eventPublisher.publish(new OrderStatusChangedEvent(
                order.getId(), user.getId(), user.getEmail(), order.getStatus()));

        log.info("Order ID {} cancelled by user ID {}", command.orderId(), command.userId());
        return null;
    }
}
