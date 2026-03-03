package com.ravn.ecommerce.application.usecases.payment;

import com.ravn.ecommerce.application.dto.response.PaymentResponse;
import com.ravn.ecommerce.application.repositories.PaymentRepository;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.payment.Payment;
import com.ravn.ecommerce.domain.model.payment.PaymentStatus;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPaymentByOrderUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private GetPaymentByOrderUseCase useCase;

    private static final Long ORDER_ID = 1L;

    private Payment buildPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(ORDER_ID);
        payment.setStripePaymentIntent("pi_123");
        payment.setClientSecret("cs_123");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(PaymentStatus.CREATED);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return payment;
    }

    @Test
    @DisplayName("Should return payment for a valid order ID")
    void shouldReturnPaymentForOrder() {
        Payment payment = buildPayment();

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        PaymentResponse response = useCase.execute(ORDER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(response.getStripePaymentIntent()).isEqualTo("pi_123");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.CREATED);

        verify(paymentRepository).findByOrderId(ORDER_ID);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no payment exists for the order")
    void shouldThrowWhenNoPaymentFoundForOrder() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ORDER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No payment found for order ID: " + ORDER_ID);

        verify(paymentRepository).findByOrderId(ORDER_ID);
    }
}
