package com.ravn.ecommerce.application.useCases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetUserOrdersUseCase implements UseCase<Long, List<OrderResponse>> {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public List<OrderResponse> execute(Long userId) {
        log.info("Fetching orders for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFound(String.format("User ID %d does not exist", userId));
        }
        return orderRepository.findAllByUserId(userId).stream()
                .map(OrderResponse::toDto)
                .toList();
    }
}
