package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.order.command.UpdateDeliveryStatusCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import com.ravn.ecommerce.domain.model.order.events.OrderStatusChangedEvent;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Updates the delivery status of an order (MANAGER only).
 *
 * Valid transitions:
 * PENDING → PAID
 * PAID → SHIPPED
 * SHIPPED → DELIVERED
 *
 * CANCELLED is a terminal state and cannot be changed.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateDeliveryStatusUseCase implements UseCase<UpdateDeliveryStatusCommand, OrderResponse> {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponse execute(UpdateDeliveryStatusCommand command) {
        log.info("Manager {} updating order {} to status {}", command.managerId(), command.orderId(),
                command.newStatus());

        // Authorization check
        User manager = userRepository.findById(command.managerId())
                .orElseThrow(() -> new EntityNotFoundException("User", command.managerId()));
        if (manager.getRole() != UserRole.MANAGER) {
            throw new UnauthorizedException("Only managers can update delivery status");
        }

        // Fetch order
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Order ID %d does not exist", command.orderId())));

        // Apply the status transition (domain methods enforce valid transitions)
        OrderStatus newStatus = command.newStatus();
        switch (newStatus) {
            case PAID -> order.markAsPaid();
            case SHIPPED -> order.markAsShipped();
            case DELIVERED -> order.markAsDelivered();
            default -> throw new IllegalArgumentException(
                    "Cannot manually set order status to: " + newStatus);
        }

        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        // Trigger email notification
        userRepository.findById(order.getUserId())
                .map(User::getEmail)
                .ifPresent(email -> eventPublisher.publish(
                        new OrderStatusChangedEvent(saved.getId(), saved.getUserId(), email, saved.getStatus())));

        log.info("Order {} status updated to {} by manager {}", saved.getId(), saved.getStatus(), command.managerId());
        return OrderResponse.toDto(saved);
    }
}
