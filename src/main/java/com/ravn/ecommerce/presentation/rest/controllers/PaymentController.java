package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.response.PaymentResponse;
import com.ravn.ecommerce.application.usecases.payment.CreatePaymentIntentUseCase;
import com.ravn.ecommerce.application.usecases.payment.GetPaymentByOrderUseCase;
import com.ravn.ecommerce.application.usecases.payment.HandleStripeWebhookUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final CreatePaymentIntentUseCase createPaymentIntentUseCase;
    private final GetPaymentByOrderUseCase getPaymentByOrderUseCase;
    private final HandleStripeWebhookUseCase handleStripeWebhookUseCase;

    @PostMapping("/orders/{orderId}/intent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@PathVariable Long orderId) {
        log.info("POST /payments/orders/{}/intent", orderId);
        PaymentResponse response = createPaymentIntentUseCase.execute(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(@PathVariable Long orderId) {
        log.info("GET /payments/orders/{}", orderId);
        PaymentResponse response = getPaymentByOrderUseCase.execute(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        log.info("POST /payments/webhook — processing Stripe event");
        handleStripeWebhookUseCase.execute(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
