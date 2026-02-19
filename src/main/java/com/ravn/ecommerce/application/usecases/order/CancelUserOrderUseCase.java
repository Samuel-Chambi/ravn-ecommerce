package com.ravn.ecommerce.application.useCases.order;

import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import com.ravn.ecommerce.application.useCases.order.command.GetUserOrderByIdCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.InvalidOrderException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.order.Order;
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

    @Override
    @Transactional
    public Void execute(GetUserOrderByIdCommand command) {
        log.info("User ID {} cancelling order ID {}", command.userId(), command.orderId());
        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFound(String.format("User ID %d does not exist", command.userId()));
        }
        Order order = orderRepository.findByIdAndUserId(command.orderId(), command.userId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Order ID %d not found for user ID %d", command.orderId(), command.userId())));
        if (!order.canBeCancelled()) {
            throw new InvalidOrderException(
                    String.format("Order ID %d cannot be cancelled (current status: %s)", command.orderId(),
                            order.getStatus()));
        }
        order.cancel();
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("Order ID {} cancelled by user ID {}", command.orderId(), command.userId());
        return null;
    }
}
