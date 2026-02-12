package com.ravn.ecommerce.presentation.filter;

import com.ravn.ecommerce.application.services.RateLimitService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getRequestURI().startsWith("/auth/reset-password")) {
            String ip = httpRequest.getRemoteAddr();
            Bucket bucket = rateLimitService.resolveBucket(ip);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (!probe.isConsumed()) {
                httpResponse.setStatus(429);
                httpResponse.getWriter().write("Too many requests");
                return;
            }

            httpResponse.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        }

        chain.doFilter(request, response);
    }
}
