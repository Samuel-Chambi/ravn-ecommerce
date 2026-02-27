package com.ravn.ecommerce.presentation.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Payments", description = "Endpoints related to payment processing, intents, and webhooks")
public class PaymentController {

    private final CreatePaymentIntentUseCase createPaymentIntentUseCase;
    private final GetPaymentByOrderUseCase getPaymentByOrderUseCase;
    private final HandleStripeWebhookUseCase handleStripeWebhookUseCase;

    @Operation(summary = "Create payment intent", description = "Creates a Stripe payment intent securely for a given order, returning the client secret needed for the frontend to complete the payment.")
    @PostMapping("/orders/{orderId}/intent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@PathVariable Long orderId) {
        log.info("POST /payments/orders/{}/intent", orderId);
        PaymentResponse response = createPaymentIntentUseCase.execute(orderId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payment by order", description = "Retrieves the payment details and status associated with a specific order.")
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(@PathVariable Long orderId) {
        log.info("GET /payments/orders/{}", orderId);
        PaymentResponse response = getPaymentByOrderUseCase.execute(orderId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Handle Stripe webhook", description = "Endpoint to receive and securely process asynchronous event notifications from Stripe (e.g., successful payment, failed payment).")
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        log.info("POST /payments/webhook — processing Stripe event");
        handleStripeWebhookUseCase.execute(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
