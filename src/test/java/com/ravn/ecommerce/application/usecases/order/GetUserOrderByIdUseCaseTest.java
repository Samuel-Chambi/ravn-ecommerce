package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.order.command.GetUserOrderByIdCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserOrderByIdUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserOrderByIdUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 10L;

    private Order buildOrder() {
        return Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return order for a valid user and order ID")
    void shouldReturnUserOrder() {
        GetUserOrderByIdCommand command = new GetUserOrderByIdCommand(USER_ID, ORDER_ID);
        Order order = buildOrder();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));

        OrderResponse result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ORDER_ID);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(userRepository).existsById(USER_ID);
        verify(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
    }

    @Test
    @DisplayName("Should throw UserNotFound when user does not exist")
    void shouldThrowWhenUserNotFound() {
        GetUserOrderByIdCommand command = new GetUserOrderByIdCommand(USER_ID, ORDER_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when order not found for user")
    void shouldThrowWhenOrderNotFoundForUser() {
        GetUserOrderByIdCommand command = new GetUserOrderByIdCommand(USER_ID, ORDER_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found for user");
    }
}
