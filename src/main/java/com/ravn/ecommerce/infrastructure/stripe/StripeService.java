package com.ravn.ecommerce.infrastructure.stripe;

import com.ravn.ecommerce.application.config.AppConfig;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Thin wrapper around the Stripe Java SDK.
 * Keeps all Stripe SDK types out of the use-case layer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final AppConfig appConfig;

    @PostConstruct
    public void init() {
        Stripe.apiKey = appConfig.getStripe().getApiKey();
        log.info("Stripe SDK initialized");
    }

    /**
     * Creates a PaymentIntent in Stripe and returns it.
     * Amount is in the smallest currency unit (cents for USD).
     */
    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency) throws StripeException {
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency.toLowerCase())
                // Only card — disables redirect-based methods (iDEAL, Bancontact, etc.)
                // so no return_url is needed when confirming
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(
                                        PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build())
                .build();

        log.info("Creating Stripe PaymentIntent for amount {} {}", amountInCents, currency);
        return PaymentIntent.create(params);
    }

    /**
     * Validates the Stripe webhook signature and parses the event.
     *
     * @throws SignatureVerificationException if the signature is invalid
     */
    public Event constructWebhookEvent(String payload, String sigHeader) throws StripeException {
        return Webhook.constructEvent(payload, sigHeader, appConfig.getStripe().getWebhookSecret());
    }

    /**
     * Creates a Refund for a given PaymentIntent in Stripe.
     * 
     * @param paymentIntentId The Stripe Payment Intent ID.
     * @return The Refund object.
     * @throws StripeException If the refund fails.
     */
    public Refund refundPayment(String paymentIntentId) throws StripeException {
        log.info("Refunding Stripe PaymentIntent {}", paymentIntentId);
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();
        return Refund.create(params);
    }
}
