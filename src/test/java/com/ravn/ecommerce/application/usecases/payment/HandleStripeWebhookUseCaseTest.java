package com.ravn.ecommerce.application.usecases.payment;

import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.PaymentRepository;
import com.ravn.ecommerce.application.repositories.StripeWebhookEventRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import com.ravn.ecommerce.domain.model.payment.Payment;
import com.ravn.ecommerce.domain.model.payment.PaymentStatus;
import com.ravn.ecommerce.domain.model.payment.StripeWebhookEvent;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import com.ravn.ecommerce.infrastructure.stripe.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandleStripeWebhookUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private StripeService stripeService;
    @Mock
    private StripeWebhookEventRepository stripeWebhookEventRepository;

    @InjectMocks
    private HandleStripeWebhookUseCase useCase;

    @Test
    @DisplayName("Should throw when Stripe signature is invalid")
    void shouldThrowWhenSignatureInvalid() throws StripeException {
        when(stripeService.constructWebhookEvent(any(), any()))
                .thenThrow(new RuntimeException("Invalid signature"));

        assertThatThrownBy(() -> useCase.execute("payload", "sig"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Stripe webhook signature");
    }

    @Test
    @DisplayName("Should skip already processed events (idempotency)")
    void shouldSkipAlreadyProcessedEvents() throws StripeException {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn("evt_123");
        when(event.getType()).thenReturn("payment_intent.succeeded");

        when(stripeService.constructWebhookEvent(any(), any())).thenReturn(event);
        when(stripeWebhookEventRepository.existsByEventId("evt_123")).thenReturn(true);

        useCase.execute("payload", "sig");

        verify(paymentRepository, never()).findByStripePaymentIntent(any());
    }

    @Test
    @DisplayName("Should handle payment_intent.succeeded event")
    void shouldHandlePaymentSucceeded() throws StripeException {
        Event event = mock(Event.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        PaymentIntent paymentIntent = mock(PaymentIntent.class);

        when(event.getId()).thenReturn("evt_456");
        when(event.getType()).thenReturn("payment_intent.succeeded");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
        when(paymentIntent.getId()).thenReturn("pi_test");

        Payment payment = Payment.builder()
                .id(1L).orderId(10L).stripePaymentIntent("pi_test")
                .amount(new BigDecimal("100.00")).status(PaymentStatus.CREATED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        Order order = Order.builder()
                .id(10L).userId(5L).status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00")).items(List.of())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        User user = User.builder().id(5L).email("user@test.com").role(UserRole.CLIENT).build();

        StripeWebhookEvent dbEvent = StripeWebhookEvent.builder()
                .id(1L).eventId("evt_456").eventType("payment_intent.succeeded")
                .processed(false).receivedAt(LocalDateTime.now())
                .build();

        when(stripeService.constructWebhookEvent(any(), any())).thenReturn(event);
        when(stripeWebhookEventRepository.existsByEventId("evt_456")).thenReturn(false);
        when(stripeWebhookEventRepository.save(any(StripeWebhookEvent.class))).thenReturn(dbEvent);
        when(paymentRepository.findByStripePaymentIntent("pi_test")).thenReturn(Optional.of(payment));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        useCase.execute("payload", "sig");

        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should handle payment_intent.payment_failed event")
    void shouldHandlePaymentFailed() throws StripeException {
        Event event = mock(Event.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        PaymentIntent paymentIntent = mock(PaymentIntent.class);

        when(event.getId()).thenReturn("evt_789");
        when(event.getType()).thenReturn("payment_intent.payment_failed");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
        when(paymentIntent.getId()).thenReturn("pi_failed");

        Payment payment = Payment.builder()
                .id(1L).orderId(10L).stripePaymentIntent("pi_failed")
                .amount(new BigDecimal("100.00")).status(PaymentStatus.CREATED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        StripeWebhookEvent dbEvent = StripeWebhookEvent.builder()
                .id(1L).eventId("evt_789").eventType("payment_intent.payment_failed")
                .processed(false).receivedAt(LocalDateTime.now())
                .build();

        when(stripeService.constructWebhookEvent(any(), any())).thenReturn(event);
        when(stripeWebhookEventRepository.existsByEventId("evt_789")).thenReturn(false);
        when(stripeWebhookEventRepository.save(any(StripeWebhookEvent.class))).thenReturn(dbEvent);
        when(paymentRepository.findByStripePaymentIntent("pi_failed")).thenReturn(Optional.of(payment));

        useCase.execute("payload", "sig");

        verify(paymentRepository).save(any(Payment.class));
    }
}
