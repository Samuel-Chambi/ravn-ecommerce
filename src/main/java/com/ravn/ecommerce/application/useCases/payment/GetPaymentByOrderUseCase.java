package com.ravn.ecommerce.application.usecases.payment;

import com.ravn.ecommerce.application.dto.response.PaymentResponse;
import com.ravn.ecommerce.application.repositories.PaymentRepository;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetPaymentByOrderUseCase {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public PaymentResponse execute(Long orderId) {
        log.info("Fetching payment for order ID {}", orderId);
        return paymentRepository.findByOrderId(orderId)
                .map(PaymentResponse::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No payment found for order ID: " + orderId));
    }
}
