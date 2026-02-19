package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.OrderRepository;
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
        order.cancel();
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Notify user
        userRepository.findById(order.getUserId()).map(User::getEmail)
                .ifPresent(email -> eventPublisher.publish(new OrderStatusChangedEvent(
                        order.getId(), order.getUserId(), email, order.getStatus())));

        log.info("Order ID {} cancelled by admin", orderId);
        return null;
    }
}
