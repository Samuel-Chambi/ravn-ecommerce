package com.ravn.ecommerce.application.usecases.refund;

import com.ravn.ecommerce.application.dto.response.RefundResponse;
import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.RefundRepository;
import com.ravn.ecommerce.application.usecases.refund.command.RequestRefundCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.InvalidOrderLogicException;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.domain.model.order.RefundStatus;
import com.ravn.ecommerce.domain.model.order.events.RefundRequestedEvent;
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
class RequestRefundUseCaseTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RequestRefundUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 10L;

    private Order buildOrder(OrderStatus status) {
        return Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(status)
                .totalAmount(new BigDecimal("100.00"))
                .items(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Refund buildSavedRefund() {
        return Refund.builder()
                .id(1L)
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .reason("Defective product")
                .status(RefundStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create refund successfully for a PAID order")
    void shouldCreateRefundSuccessfully() {
        Order order = buildOrder(OrderStatus.PAID);
        Refund savedRefund = buildSavedRefund();
        RequestRefundCommand command = new RequestRefundCommand(USER_ID, ORDER_ID, "Defective product");

        when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(refundRepository.existsActiveByOrderId(ORDER_ID)).thenReturn(false);
        when(refundRepository.save(any(Refund.class))).thenReturn(savedRefund);

        RefundResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(response.getStatus()).isEqualTo(RefundStatus.PENDING);
        verify(refundRepository).save(any(Refund.class));
    }

    @Test
    @DisplayName("Should throw when order not found for user")
    void shouldThrowWhenOrderNotFound() {
        RequestRefundCommand command = new RequestRefundCommand(USER_ID, ORDER_ID, "Defective product");

        when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order ID");
    }

    @Test
    @DisplayName("Should throw when order is not eligible for refund (PENDING status)")
    void shouldThrowWhenOrderNotEligible() {
        Order pendingOrder = buildOrder(OrderStatus.PENDING);
        RequestRefundCommand command = new RequestRefundCommand(USER_ID, ORDER_ID, "Changed my mind");

        when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidOrderLogicException.class)
                .hasMessageContaining("not eligible");
    }

    @Test
    @DisplayName("Should throw when active refund already exists (duplicate prevention)")
    void shouldThrowWhenActiveRefundExists() {
        Order paidOrder = buildOrder(OrderStatus.PAID);
        RequestRefundCommand command = new RequestRefundCommand(USER_ID, ORDER_ID, "Defective product");

        when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(paidOrder));
        when(refundRepository.existsActiveByOrderId(ORDER_ID)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidOrderLogicException.class)
                .hasMessageContaining("already has a pending or approved");
    }

    @Test
    @DisplayName("Should publish RefundRequestedEvent after creating refund")
    void shouldPublishRefundRequestedEvent() {
        Order order = buildOrder(OrderStatus.DELIVERED);
        Refund savedRefund = buildSavedRefund();
        RequestRefundCommand command = new RequestRefundCommand(USER_ID, ORDER_ID, "Wrong size");

        when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.of(order));
        when(refundRepository.existsActiveByOrderId(ORDER_ID)).thenReturn(false);
        when(refundRepository.save(any(Refund.class))).thenReturn(savedRefund);

        useCase.execute(command);

        verify(eventPublisher).publish(any(RefundRequestedEvent.class));
    }
}
