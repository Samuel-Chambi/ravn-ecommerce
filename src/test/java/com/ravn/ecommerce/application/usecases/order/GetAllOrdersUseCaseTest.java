package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.usecases.order.command.GetAllOrdersCommand;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllOrdersUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private GetAllOrdersUseCase useCase;

    private Order buildOrder(Long id, Long userId) {
        return Order.builder()
                .id(id)
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return all orders with cursor-based pagination")
    void shouldReturnAllOrders() {
        ScrollPosition position = ScrollPosition.offset();
        Limit limit = Limit.of(10);
        GetAllOrdersCommand command = new GetAllOrdersCommand(position, limit);

        Order order1 = buildOrder(1L, 1L);
        Order order2 = buildOrder(2L, 2L);
        List<Order> orders = List.of(order1, order2);
        Window<Order> orderWindow = Window.from(orders, ScrollPosition::offset);

        when(orderRepository.findAll(position, limit)).thenReturn(orderWindow);

        Window<OrderResponse> result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(1).getId()).isEqualTo(2L);
        verify(orderRepository).findAll(position, limit);
    }
}
