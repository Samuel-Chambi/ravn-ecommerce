package com.ravn.ecommerce.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Disable CSRF for testing (re-enable in production)
                                .csrf(csrf -> csrf.disable())
                                // Disable CORS for testing (configure properly in production)
                                .cors(cors -> cors.disable())
                                // Stateless session management
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // Authorize requests
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/test/**", "/auth/**", "/public/**").permitAll()
                                                // Allow product image endpoints
                                                .requestMatchers("/products/*/images", "/products/*/images/**")
                                                .permitAll()
                                                // Allow serving static images
                                                .requestMatchers("/uploads/**").permitAll()
                                                .anyRequest().authenticated());

                return http.build();
        }
}
