package com.ravn.ecommerce.application.usecases.auth;

import com.ravn.ecommerce.infrastructure.security.JwtService;
import com.ravn.ecommerce.infrastructure.security.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignOutUseCaseTest {

    private static final String RAW_TOKEN = "valid-jwt-token";
    private static final String BEARER_TOKEN = "Bearer " + RAW_TOKEN;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private Claims claims;

    @InjectMocks
    private SignOutUseCase signOutUseCase;

    @Test
    @DisplayName("Should blacklist token successfully with Bearer prefix")
    void shouldBlacklistTokenWithBearerPrefix() {
        Date futureExpiration = new Date(System.currentTimeMillis() + 60000);
        when(jwtService.extractAllClaims(RAW_TOKEN)).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(futureExpiration);

        signOutUseCase.execute(BEARER_TOKEN);

        verify(tokenBlacklistService).banToken(eq(RAW_TOKEN), anyLong());
    }

    @Test
    @DisplayName("Should blacklist token successfully without Bearer prefix")
    void shouldBlacklistTokenWithoutBearerPrefix() {
        Date futureExpiration = new Date(System.currentTimeMillis() + 60000);
        when(jwtService.extractAllClaims(RAW_TOKEN)).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(futureExpiration);

        signOutUseCase.execute(RAW_TOKEN);

        verify(tokenBlacklistService).banToken(eq(RAW_TOKEN), anyLong());
    }

    @Test
    @DisplayName("Should not blacklist token when TTL is zero or negative")
    void shouldNotBlacklistExpiredToken() {
        Date pastExpiration = new Date(System.currentTimeMillis() - 60000);
        when(jwtService.extractAllClaims(RAW_TOKEN)).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(pastExpiration);

        signOutUseCase.execute(BEARER_TOKEN);

        verify(tokenBlacklistService, never()).banToken(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should handle exception gracefully during sign out")
    void shouldHandleExceptionGracefully() {
        when(jwtService.extractAllClaims(RAW_TOKEN)).thenThrow(new RuntimeException("Invalid token"));

        signOutUseCase.execute(BEARER_TOKEN);

        verify(tokenBlacklistService, never()).banToken(anyString(), anyLong());
    }
}
