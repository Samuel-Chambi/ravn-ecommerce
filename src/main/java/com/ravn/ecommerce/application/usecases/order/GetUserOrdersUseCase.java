package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import com.ravn.ecommerce.application.usecases.order.command.GetUserOrdersCommand;
import org.springframework.data.domain.Window;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetUserOrdersUseCase implements UseCase<GetUserOrdersCommand, Window<OrderResponse>> {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public Window<OrderResponse> execute(GetUserOrdersCommand command) {
        log.info("Fetching orders for user ID: {}", command.userId());
        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFound(String.format("User ID %d does not exist", command.userId()));
        }
        return orderRepository.findAllByUserId(command.userId(), command.position(), command.limit())
                .map(OrderResponse::toDto);
    }
}
