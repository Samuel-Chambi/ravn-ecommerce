package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import com.ravn.ecommerce.application.usecases.order.command.GetAllOrdersCommand;
import org.springframework.data.domain.Window;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetAllOrdersUseCase implements UseCase<GetAllOrdersCommand, Window<OrderResponse>> {
    private final OrderRepository orderRepository;

    @Override
    public Window<OrderResponse> execute(GetAllOrdersCommand command) {
        log.info("Fetching all orders (admin)");
        return orderRepository.findAll(command.position(), command.limit())
                .map(OrderResponse::toDto);
    }
}
