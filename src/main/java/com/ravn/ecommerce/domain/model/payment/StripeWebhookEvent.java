package com.ravn.ecommerce.domain.model.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StripeWebhookEvent {
    private Long id;
    private String eventId;
    private String eventType;
    private String payload;
    private boolean processed;
    private LocalDateTime receivedAt;

    public void markAsProcessed() {
        this.processed = true;
    }
}
