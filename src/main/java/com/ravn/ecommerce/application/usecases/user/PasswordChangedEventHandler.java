package com.ravn.ecommerce.application.usecases.user;

import com.ravn.ecommerce.domain.model.user.events.PasswordChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordChangedEventHandler {

    private final SendPasswordChangeEmailUseCase sendPasswordChangeEmailUseCase;

    @Async
    @EventListener
    public void handle(PasswordChangedEvent event) {
        log.info("Handling PasswordChangedEvent for user {}", event.getUserId());
        sendPasswordChangeEmailUseCase.execute(event.getEmail(), event.getOccurredOn());
    }
}
