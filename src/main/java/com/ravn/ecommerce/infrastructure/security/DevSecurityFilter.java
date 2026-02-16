package com.ravn.ecommerce.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Temporary filter for development to simulate authentication via header.
 * Reads 'X-User-Id' header and sets a dummy authentication in SecurityContext.
 */
@Component
public class DevSecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userIdHeader = request.getHeader("X-User-Id");

        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                Long userId = Long.parseLong(userIdHeader);

                // Create a dummy principal with the ID
                // Ideally this would load from DB, but for now we just need the ID to pass to
                // UseCases
                // Assigning ROLE_MANAGER to allow passing security checks if any
                UserDetails principal = new User(userId.toString(), "password",
                        List.of(new SimpleGrantedAuthority("MANAGER")));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal,
                        null, principal.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (NumberFormatException e) {
                // Ignore invalid format, proceed unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}
