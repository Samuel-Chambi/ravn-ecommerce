package com.ravn.ecommerce.application.usecases.refund;

import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.refund.command.RejectRefundCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.application.repositories.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RejectRefundUseCase implements UseCase<RejectRefundCommand, Void> {

    private final RefundRepository refundRepository;
    // Opcional: private final EventPublisher eventPublisher;

    @Override
    public Void execute(RejectRefundCommand command) {
        log.info("Manager is rejecting refund request ID {} with note: {}", command.refundRequestId(),
                command.adminNote());

        Refund refund = refundRepository.findById(command.refundRequestId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("RefundRequest ID %d not found", command.refundRequestId())));

        refund.reject(command.adminNote());
        refundRepository.save(refund);

        log.info("Refund request ID {} REJECTED.", refund.getId());

        // TODO: In the future, publish an event like RefundRejectedEvent to notify the
        // user via Email

        return null;
    }
}
