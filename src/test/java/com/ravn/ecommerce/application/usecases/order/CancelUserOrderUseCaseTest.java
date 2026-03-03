package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.PaymentRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.order.command.GetUserOrderByIdCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.InvalidOrderException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderItem;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import com.ravn.ecommerce.domain.model.product.Inventory;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import com.ravn.ecommerce.infrastructure.stripe.StripeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelUserOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StripeService stripeService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CancelUserOrderUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 10L;
    private static final Long PRODUCT_ID = 20L;

    private User buildUser() {
        return User.builder()
                .id(USER_ID)
                .email("user@test.com")
                .passwordHash("hashed")
                .role(UserRole.CLIENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private OrderItem buildOrderItem() {
        return OrderItem.builder()
                .id(1L)
                .orderId(ORDER_ID)
                .productId(PRODUCT_ID)
                .quantity(2)
                .price(new BigDecimal("50.00"))
                .productName("Test Product")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Product buildProduct(int stock) {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .price(new BigDecimal("50.00"))
                .isEnabled(true)
                .inventory(Inventory.builder()
                        .id(1L)
                        .productId(PRODUCT_ID)
                        .quantity(stock)
                        .updatedAt(LocalDateTime.now())
                        .build())
                .build();
    }

    private Order buildOrder(OrderStatus status) {
        return Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(status)
                .totalAmount(new BigDecimal("100.00"))
                .items(List.of(buildOrderItem()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should cancel user order successfully when order is PENDING")
    void shouldCancelUserOrderSuccessfully() {
        GetUserOrderByIdCommand command = new GetUserOrderByIdCommand(USER_ID, ORDER_ID);
        User user = buildUser();
        Order order = buildOrder(OrderStatus.PENDING);
        Product product = buildProduct(10);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        useCase.execute(command);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getInventory().getQuantity()).isEqualTo(12);
        verify(orderRepository).save(any(Order.class));
        verify(productRepository).save(any(Product.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should throw UserNotFound when user does not exist")
    void shouldThrowWhenUserNotFound() {
        GetUserOrderByIdCommand command = new GetUserOrderByIdCommand(USER_ID, ORDER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when order not found for user")
    void shouldThrowWhenOrderNotFoundForUser() {
        GetUserOrderByIdCommand command = new GetUserOrderByIdCommand(USER_ID, ORDER_ID);
        User user = buildUser();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found for user");
    }

    @Test
    @DisplayName("Should throw InvalidOrderException when order cannot be cancelled")
    void shouldThrowWhenOrderCannotBeCancelled() {
        GetUserOrderByIdCommand command = new GetUserOrderByIdCommand(USER_ID, ORDER_ID);
        User user = buildUser();
        Order paidOrder = buildOrder(OrderStatus.PAID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(paidOrder));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("cannot be cancelled");
    }
}
