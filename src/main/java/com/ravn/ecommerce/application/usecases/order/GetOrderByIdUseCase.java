package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetOrderByIdUseCase implements UseCase<Long, OrderResponse> {
    private final OrderRepository orderRepository;

    @Override
    public OrderResponse execute(Long orderId) {
        log.info("Fetching order ID: {} (admin)", orderId);
        return orderRepository.findById(orderId)
                .map(OrderResponse::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Order ID %d does not exist", orderId)));
    }
}
