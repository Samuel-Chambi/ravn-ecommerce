package com.ravn.ecommerce.application.usecases.refund;

import com.ravn.ecommerce.application.dto.response.RefundResponse;
import com.ravn.ecommerce.application.repositories.RefundRepository;
import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.domain.model.order.RefundStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPendingRefundsUseCaseTest {

    @Mock
    private RefundRepository repository;

    @InjectMocks
    private GetPendingRefundsUseCase useCase;

    private Refund buildRefund(Long id, Long orderId) {
        return Refund.builder()
                .id(id)
                .orderId(orderId)
                .userId(1L)
                .reason("Defective")
                .status(RefundStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return all pending refunds")
    void shouldReturnPendingRefunds() {
        List<Refund> pendingRefunds = List.of(
                buildRefund(1L, 10L),
                buildRefund(2L, 20L)
        );

        when(repository.findByStatus(RefundStatus.PENDING)).thenReturn(pendingRefunds);

        List<RefundResponse> responses = useCase.execute(null);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getStatus()).isEqualTo(RefundStatus.PENDING);
        assertThat(responses.get(1).getStatus()).isEqualTo(RefundStatus.PENDING);
    }

    @Test
    @DisplayName("Should return empty list when no pending refunds exist")
    void shouldReturnEmptyListWhenNoPendingRefunds() {
        when(repository.findByStatus(RefundStatus.PENDING)).thenReturn(List.of());

        List<RefundResponse> responses = useCase.execute(null);

        assertThat(responses).isEmpty();
    }
}
