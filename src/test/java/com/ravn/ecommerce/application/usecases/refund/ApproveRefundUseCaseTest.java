package com.ravn.ecommerce.application.usecases.refund;

import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.*;
import com.ravn.ecommerce.application.usecases.refund.command.ApproveRefundCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderItem;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.domain.model.order.RefundStatus;
import com.ravn.ecommerce.domain.model.payment.Payment;
import com.ravn.ecommerce.domain.model.payment.PaymentStatus;
import com.ravn.ecommerce.domain.model.product.Inventory;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import com.ravn.ecommerce.infrastructure.stripe.StripeService;
import com.stripe.exception.StripeException;
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
class ApproveRefundUseCaseTest {

    @Mock
    private RefundRepository refundRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StripeService stripeService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ApproveRefundUseCase useCase;

    private static final Long REFUND_ID = 1L;
    private static final Long ORDER_ID = 10L;
    private static final Long USER_ID = 5L;
    private static final Long PRODUCT_ID = 20L;

    private Refund buildRefund() {
        return Refund.builder()
                .id(REFUND_ID)
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .reason("Defective product")
                .status(RefundStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Order buildOrder() {
        return Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(OrderStatus.PAID)
                .totalAmount(new BigDecimal("100.00"))
                .items(List.of(OrderItem.builder()
                        .id(1L)
                        .orderId(ORDER_ID)
                        .productId(PRODUCT_ID)
                        .quantity(2)
                        .price(new BigDecimal("50.00"))
                        .productName("Test Product")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Payment buildPayment() {
        return Payment.builder()
                .id(1L)
                .orderId(ORDER_ID)
                .stripePaymentIntent("pi_test123")
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.SUCCEEDED)
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

    @Test
    @DisplayName("Should approve refund successfully and restock inventory")
    void shouldApproveRefundSuccessfully() throws StripeException {
        Refund refund = buildRefund();
        Order order = buildOrder();
        Payment payment = buildPayment();
        Product product = buildProduct(10);
        User user = User.builder().id(USER_ID).email("user@test.com").role(UserRole.CLIENT).build();
        ApproveRefundCommand command = new ApproveRefundCommand(REFUND_ID, "Approved by admin");

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        useCase.execute(command);

        verify(stripeService).refundPayment("pi_test123");
        verify(paymentRepository).save(any(Payment.class));
        verify(refundRepository).save(any(Refund.class));
        verify(orderRepository).save(any(Order.class));
        verify(productRepository).save(any(Product.class));
        assertThat(product.getInventory().getQuantity()).isEqualTo(12);
    }

    @Test
    @DisplayName("Should throw when refund not found")
    void shouldThrowWhenRefundNotFound() {
        ApproveRefundCommand command = new ApproveRefundCommand(REFUND_ID, "Note");

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("RefundRequest ID");
    }

    @Test
    @DisplayName("Should throw when order not found")
    void shouldThrowWhenOrderNotFound() {
        Refund refund = buildRefund();
        ApproveRefundCommand command = new ApproveRefundCommand(REFUND_ID, "Note");

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order ID");
    }

    @Test
    @DisplayName("Should throw when payment not found")
    void shouldThrowWhenPaymentNotFound() {
        Refund refund = buildRefund();
        Order order = buildOrder();
        ApproveRefundCommand command = new ApproveRefundCommand(REFUND_ID, "Note");

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No payment found");
    }

    @Test
    @DisplayName("Should throw RuntimeException when Stripe refund fails")
    void shouldThrowWhenStripeRefundFails() throws StripeException {
        Refund refund = buildRefund();
        Order order = buildOrder();
        Payment payment = buildPayment();
        ApproveRefundCommand command = new ApproveRefundCommand(REFUND_ID, "Note");

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
        doThrow(new RuntimeException("Stripe error")).when(stripeService).refundPayment(any());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process refund");
    }
}
