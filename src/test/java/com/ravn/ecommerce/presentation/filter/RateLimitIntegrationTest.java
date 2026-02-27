package com.ravn.ecommerce.presentation.filter;

import com.ravn.ecommerce.application.services.RateLimitService;
import com.ravn.ecommerce.domain.exceptions.TooManyRequestsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RRateLimiter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;
    @Mock
    private HandlerExceptionResolver resolver;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private RRateLimiter rateLimiter;

    @Test
    @DisplayName("Should allow request when not rate limited")
    void shouldAllowWhenNotRateLimited() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(rateLimitService, resolver);

        when(request.getRequestURI()).thenReturn("/auth/reset-password");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimitService.getRateLimiter("rate_limit:reset_pwd:127.0.0.1")).thenReturn(rateLimiter);
        when(rateLimiter.tryAcquire(1)).thenReturn(true);
        when(rateLimiter.availablePermits()).thenReturn(4L);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should block request when rate limit exceeded")
    void shouldBlockWhenRateLimitExceeded() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(rateLimitService, resolver);

        when(request.getRequestURI()).thenReturn("/auth/reset-password");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimitService.getRateLimiter("rate_limit:reset_pwd:127.0.0.1")).thenReturn(rateLimiter);
        when(rateLimiter.tryAcquire(1)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        verify(resolver).resolveException(eq(request), eq(response), isNull(), any(TooManyRequestsException.class));
    }

    @Test
    @DisplayName("Should pass through for non-rate-limited endpoints")
    void shouldPassThroughForOtherEndpoints() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(rateLimitService, resolver);

        when(request.getRequestURI()).thenReturn("/products");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimitService);
    }
}
