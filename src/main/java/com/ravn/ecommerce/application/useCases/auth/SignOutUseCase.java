package com.ravn.ecommerce.application.usecases.auth;

import com.ravn.ecommerce.application.usecases.UseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SignOutUseCase implements UseCase<Void, String> {
    @Override
    public String execute(Void input) {
        // JWT is stateless — actual logout is handled by the client discarding the
        // token.
        // Future: add token to a redis blacklist here.
        log.info("Sign out called — client should discard the token");
        return "Signed out successfully";
    }
}
