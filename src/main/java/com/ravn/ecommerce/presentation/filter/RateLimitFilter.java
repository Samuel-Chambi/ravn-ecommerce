package com.ravn.ecommerce.presentation.filter;

import com.ravn.ecommerce.application.services.RateLimitService;
import org.redisson.api.RRateLimiter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import com.ravn.ecommerce.domain.exceptions.TooManyRequestsException;

import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {

    private final RateLimitService rateLimitService;
    private final HandlerExceptionResolver resolver;

    public RateLimitFilter(RateLimitService rateLimitService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.rateLimitService = rateLimitService;
        this.resolver = resolver;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getRequestURI().startsWith("/auth/reset-password")) {
            String ip = httpRequest.getRemoteAddr();
            RRateLimiter rateLimiter = rateLimitService.getRateLimiter("rate_limit:reset_pwd:" + ip);

            if (!rateLimiter.tryAcquire(1)) {
                resolver.resolveException(httpRequest, httpResponse, null,
                        new TooManyRequestsException("Too many reset attempts. Please wait before trying again."));
                return;
            }

            httpResponse.addHeader("X-Rate-Limit-Remaining", String.valueOf(rateLimiter.availablePermits()));
        }

        chain.doFilter(request, response);
    }
}
