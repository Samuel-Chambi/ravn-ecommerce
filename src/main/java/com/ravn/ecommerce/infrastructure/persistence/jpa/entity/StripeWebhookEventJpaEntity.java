package com.ravn.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "stripe_webhook_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeWebhookEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    // We store the raw JSON payload
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(name = "processed", nullable = false)
    private boolean processed;

    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;
}
