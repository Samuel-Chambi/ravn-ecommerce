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
class GetUserRefundsUseCaseTest {

    @Mock
    private RefundRepository repository;

    @InjectMocks
    private GetUserRefundsUseCase useCase;

    private static final Long USER_ID = 1L;

    private Refund buildRefund(Long id, RefundStatus status) {
        return Refund.builder()
                .id(id)
                .orderId(10L)
                .userId(USER_ID)
                .reason("Defective product")
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return all refunds for user")
    void shouldReturnUserRefunds() {
        List<Refund> userRefunds = List.of(
                buildRefund(1L, RefundStatus.PENDING),
                buildRefund(2L, RefundStatus.APPROVED)
        );

        when(repository.findByUserId(USER_ID)).thenReturn(userRefunds);

        List<RefundResponse> responses = useCase.execute(USER_ID);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getUserId()).isEqualTo(USER_ID);
        assertThat(responses.get(1).getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("Should return empty list when user has no refunds")
    void shouldReturnEmptyListWhenNoRefunds() {
        when(repository.findByUserId(USER_ID)).thenReturn(List.of());

        List<RefundResponse> responses = useCase.execute(USER_ID);

        assertThat(responses).isEmpty();
    }
}
