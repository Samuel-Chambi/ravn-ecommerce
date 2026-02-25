package com.ravn.ecommerce.application.usecases.auth;

import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.infrastructure.security.JwtService;
import com.ravn.ecommerce.infrastructure.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignOutUseCase implements UseCase<String, Void> {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public Void execute(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            Claims claims = jwtService.extractAllClaims(token);
            Date expiration = claims.getExpiration();
            long timeToLiveMillis = expiration.getTime() - System.currentTimeMillis();

            if (timeToLiveMillis > 0) {
                tokenBlacklistService.banToken(token, timeToLiveMillis);
            }
            log.info("Token successfully added to blacklist.");
        } catch (Exception e) {
            log.warn("Could not blacklist token during sign out: {}", e.getMessage());
        }

        return null;
    }
}
