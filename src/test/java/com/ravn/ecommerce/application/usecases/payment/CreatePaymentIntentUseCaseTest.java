package com.ravn.ecommerce.application.usecases.payment;

import com.ravn.ecommerce.application.dto.response.PaymentResponse;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.PaymentRepository;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import com.ravn.ecommerce.domain.model.payment.Payment;
import com.ravn.ecommerce.domain.model.payment.PaymentStatus;
import com.ravn.ecommerce.infrastructure.stripe.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePaymentIntentUseCaseTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private StripeService stripeService;

    @InjectMocks
    private CreatePaymentIntentUseCase useCase;

    private static final Long ORDER_ID = 1L;
    private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("100.00");

    private Order buildOrder() {
        return Order.builder()
                .id(ORDER_ID)
                .userId(10L)
                .status(OrderStatus.PENDING)
                .totalAmount(TOTAL_AMOUNT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Payment buildPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(ORDER_ID);
        payment.setStripePaymentIntent("pi_123");
        payment.setClientSecret("cs_123");
        payment.setAmount(TOTAL_AMOUNT);
        payment.setStatus(PaymentStatus.CREATED);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return payment;
    }

    @Test
    @DisplayName("Should create payment intent successfully for a valid order")
    void shouldCreatePaymentIntentSuccessfully() throws StripeException {
        Order order = buildOrder();
        Payment payment = buildPayment();

        PaymentIntent stripeIntent = mock(PaymentIntent.class);
        when(stripeIntent.getId()).thenReturn("pi_123");
        when(stripeIntent.getClientSecret()).thenReturn("cs_123");

        when(paymentRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(stripeService.createPaymentIntent(TOTAL_AMOUNT, "usd")).thenReturn(stripeIntent);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = useCase.execute(ORDER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(response.getStripePaymentIntent()).isEqualTo("pi_123");
        assertThat(response.getAmount()).isEqualByComparingTo(TOTAL_AMOUNT);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.CREATED);

        verify(paymentRepository).save(any(Payment.class));
        verify(stripeService).createPaymentIntent(TOTAL_AMOUNT, "usd");
    }

    @Test
    @DisplayName("Should return existing payment when payment already exists for order (idempotency)")
    void shouldReturnExistingPaymentWhenAlreadyExists() throws StripeException {
        Payment existingPayment = buildPayment();

        when(paymentRepository.existsByOrderId(ORDER_ID)).thenReturn(true);
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(existingPayment));

        PaymentResponse response = useCase.execute(ORDER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(response.getStripePaymentIntent()).isEqualTo("pi_123");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.CREATED);

        verify(orderRepository, never()).findById(any());
        verify(stripeService, never()).createPaymentIntent(any(), any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when order does not exist")
    void shouldThrowWhenOrderNotFound() throws StripeException {
        when(paymentRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ORDER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order not found: " + ORDER_ID);

        verify(stripeService, never()).createPaymentIntent(any(), any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolation when Stripe service fails")
    void shouldThrowBusinessRuleViolationWhenStripeFails() throws StripeException {
        Order order = buildOrder();

        when(paymentRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(stripeService.createPaymentIntent(eq(TOTAL_AMOUNT), eq("usd")))
                .thenThrow(new RuntimeException("Stripe API is unavailable"));

        assertThatThrownBy(() -> useCase.execute(ORDER_ID))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessageContaining("Failed to create payment intent");

        verify(paymentRepository, never()).save(any());
    }
}
