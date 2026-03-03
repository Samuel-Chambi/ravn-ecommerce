package com.ravn.ecommerce.application.usecases.refund;

import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.RefundRepository;
import com.ravn.ecommerce.application.usecases.refund.command.RejectRefundCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.domain.model.order.RefundStatus;
import com.ravn.ecommerce.domain.model.order.events.RefundRejectedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RejectRefundUseCaseTest {

    @Mock
    private RefundRepository refundRepository;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RejectRefundUseCase useCase;

    private static final Long REFUND_ID = 1L;

    private Refund buildRefund() {
        return Refund.builder()
                .id(REFUND_ID)
                .orderId(10L)
                .userId(5L)
                .reason("Defective product")
                .status(RefundStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should reject refund successfully")
    void shouldRejectRefundSuccessfully() {
        Refund refund = buildRefund();
        RejectRefundCommand command = new RejectRefundCommand(REFUND_ID, "Not eligible for refund");

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));

        useCase.execute(command);

        verify(refundRepository).save(any(Refund.class));
        verify(eventPublisher).publish(any(RefundRejectedEvent.class));
    }

    @Test
    @DisplayName("Should throw when refund not found")
    void shouldThrowWhenRefundNotFound() {
        RejectRefundCommand command = new RejectRefundCommand(REFUND_ID, "Not eligible");

        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("RefundRequest ID");
    }
}
