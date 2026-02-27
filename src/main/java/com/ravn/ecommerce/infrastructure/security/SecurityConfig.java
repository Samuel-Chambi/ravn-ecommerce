package com.ravn.ecommerce.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/graphiql/**").permitAll()
                        // Swagger UI & API Docs
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // GraphQL endpoint: security is enforced at method level via @PreAuthorize
                        .requestMatchers("/graphql").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/manage/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()

                        // Manager-only: product writes
                        .requestMatchers(HttpMethod.POST, "/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/products/**").hasRole("MANAGER")

                        // User-scoped (authenticated, CLIENT + MANAGER) routes MUST be defined before
                        // more general /orders rules
                        .requestMatchers("/orders/me/**").authenticated()
                        .requestMatchers("/orders/me").authenticated()

                        // Manager-only: admin order management
                        .requestMatchers(HttpMethod.GET, "/orders").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/orders/{orderId}").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/orders/{orderId}/cancel").hasRole("MANAGER")

                        // Refunds
                        .requestMatchers(HttpMethod.POST, "/refunds").authenticated()
                        .requestMatchers(HttpMethod.GET, "/refunds/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/refunds/pending").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/refunds/{refundId}/approve").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/refunds/{refundId}/reject").hasRole("MANAGER")

                        // Payments webhooks
                        .requestMatchers(HttpMethod.POST, "/payments/webhook").permitAll()

                        // Other User-scoped services
                        .requestMatchers("/payments/**").authenticated()
                        .requestMatchers("/addresses/**").authenticated()
                        .requestMatchers("/cart/**").authenticated()
                        .requestMatchers("/favorites/**").authenticated()

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
