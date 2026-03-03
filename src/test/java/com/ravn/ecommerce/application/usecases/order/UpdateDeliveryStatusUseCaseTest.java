package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.order.command.UpdateDeliveryStatusCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateDeliveryStatusUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private UpdateDeliveryStatusUseCase useCase;

    private static final Long ORDER_ID = 1L;
    private static final Long MANAGER_ID = 100L;
    private static final Long USER_ID = 50L;

    private User buildManager() {
        return User.builder()
                .id(MANAGER_ID)
                .email("manager@test.com")
                .passwordHash("hashed")
                .role(UserRole.MANAGER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private User buildClient() {
        return User.builder()
                .id(MANAGER_ID)
                .email("client@test.com")
                .passwordHash("hashed")
                .role(UserRole.CLIENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private User buildOrderOwner() {
        return User.builder()
                .id(USER_ID)
                .email("owner@test.com")
                .passwordHash("hashed")
                .role(UserRole.CLIENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Order buildOrder(OrderStatus status) {
        return Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(status)
                .totalAmount(new BigDecimal("100.00"))
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should update order status to PAID successfully when order is PENDING")
    void shouldUpdateStatusToPaidSuccessfully() {
        UpdateDeliveryStatusCommand command = new UpdateDeliveryStatusCommand(ORDER_ID, MANAGER_ID, OrderStatus.PAID);
        User manager = buildManager();
        Order order = buildOrder(OrderStatus.PENDING);
        User orderOwner = buildOrderOwner();

        when(userRepository.findById(MANAGER_ID)).thenReturn(Optional.of(manager));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(orderOwner));

        OrderResponse result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when manager does not exist")
    void shouldThrowWhenManagerNotFound() {
        UpdateDeliveryStatusCommand command = new UpdateDeliveryStatusCommand(ORDER_ID, MANAGER_ID, OrderStatus.PAID);

        when(userRepository.findById(MANAGER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user is not a manager")
    void shouldThrowWhenManagerIsNotManager() {
        UpdateDeliveryStatusCommand command = new UpdateDeliveryStatusCommand(ORDER_ID, MANAGER_ID, OrderStatus.PAID);
        User client = buildClient();

        when(userRepository.findById(MANAGER_ID)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only managers");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when order does not exist")
    void shouldThrowWhenOrderNotFound() {
        UpdateDeliveryStatusCommand command = new UpdateDeliveryStatusCommand(ORDER_ID, MANAGER_ID, OrderStatus.PAID);
        User manager = buildManager();

        when(userRepository.findById(MANAGER_ID)).thenReturn(Optional.of(manager));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("does not exist");
    }
}
