package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.order.command.GetUserOrdersCommand;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserOrdersUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserOrdersUseCase useCase;

    private static final Long USER_ID = 1L;

    private Order buildOrder(Long orderId) {
        return Order.builder()
                .id(orderId)
                .userId(USER_ID)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return orders for a valid user")
    void shouldReturnUserOrders() {
        ScrollPosition position = ScrollPosition.offset();
        Limit limit = Limit.of(10);
        GetUserOrdersCommand command = new GetUserOrdersCommand(USER_ID, position, limit);

        Order order1 = buildOrder(1L);
        Order order2 = buildOrder(2L);
        List<Order> orders = List.of(order1, order2);
        Window<Order> orderWindow = Window.from(orders, ScrollPosition::offset);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(orderRepository.findAllByUserId(USER_ID, position, limit)).thenReturn(orderWindow);

        Window<OrderResponse> result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(USER_ID);
        assertThat(result.getContent().get(1).getUserId()).isEqualTo(USER_ID);
        verify(userRepository).existsById(USER_ID);
        verify(orderRepository).findAllByUserId(USER_ID, position, limit);
    }

    @Test
    @DisplayName("Should throw UserNotFound when user does not exist")
    void shouldThrowWhenUserNotFound() {
        ScrollPosition position = ScrollPosition.offset();
        Limit limit = Limit.of(10);
        GetUserOrdersCommand command = new GetUserOrdersCommand(USER_ID, position, limit);

        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining("does not exist");
    }
}
