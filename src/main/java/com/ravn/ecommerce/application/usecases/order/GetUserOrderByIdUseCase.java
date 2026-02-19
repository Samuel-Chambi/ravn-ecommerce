package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.order.command.GetUserOrderByIdCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetUserOrderByIdUseCase implements UseCase<GetUserOrderByIdCommand, OrderResponse> {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public OrderResponse execute(GetUserOrderByIdCommand command) {
        log.info("Fetching order ID: {} for user ID: {}", command.orderId(), command.userId());
        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFound(String.format("User ID %d does not exist", command.userId()));
        }
        return orderRepository.findByIdAndUserId(command.orderId(), command.userId())
                .map(OrderResponse::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Order ID %d not found for user ID %d", command.orderId(), command.userId())));
    }
}
