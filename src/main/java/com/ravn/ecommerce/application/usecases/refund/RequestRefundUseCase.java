package com.ravn.ecommerce.application.usecases.refund;

import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.dto.response.RefundResponse;
import com.ravn.ecommerce.application.usecases.refund.command.RequestRefundCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.InvalidOrderLogicException;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestRefundUseCase implements UseCase<RequestRefundCommand, RefundResponse> {

    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    // Optional: private final EventPublisher eventPublisher;

    @Override
    public RefundResponse execute(RequestRefundCommand command) {
        log.info("User {} is requesting a refund for Order {}", command.userId(), command.orderId());

        Order order = orderRepository.findByIdAndUserId(command.orderId(), command.userId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Order ID %d not found for User ID %d", command.orderId(), command.userId())));

        if (!order.canRequestRefund()) {
            throw new InvalidOrderLogicException(
                    String.format(
                            "Order ID %d with status %s is not eligible for a refund request. Only PAID, SHIPPED, or DELIVERED orders can be refunded.",
                            order.getId(), order.getStatus()));
        }

        Refund newRequest = Refund.createPending(order.getId(), command.userId(), command.reason());
        Refund savedRequest = refundRepository.save(newRequest);

        log.info("Refund request created successfully with ID {} for Order ID {}", savedRequest.getId(), order.getId());

        // TODO: In the future, publish an event like RefundRequestedEvent to notify
        // admins via Email

        return RefundResponse.toDto(savedRequest);
    }
}
