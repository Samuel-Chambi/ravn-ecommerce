package com.ravn.ecommerce.domain.model.user.events;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PasswordChangedEvent {
    private final Long userId;
    private final String email;
    private final LocalDateTime occurredOn;

    public PasswordChangedEvent(Long userId, String email) {
        this.userId = userId;
        this.email = email;
        this.occurredOn = LocalDateTime.now();
    }
}
