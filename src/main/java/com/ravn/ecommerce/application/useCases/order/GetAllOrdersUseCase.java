package com.ravn.ecommerce.application.useCases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetAllOrdersUseCase implements UseCase<Void, List<OrderResponse>> {
    private final OrderRepository orderRepository;

    @Override
    public List<OrderResponse> execute(Void input) {
        log.info("Fetching all orders (admin)");
        return orderRepository.findAll().stream()
                .map(OrderResponse::toDto)
                .toList();
    }
}
