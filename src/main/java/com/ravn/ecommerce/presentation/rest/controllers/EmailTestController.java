package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping("/send-email")
    public ResponseEntity<Map<String, String>> sendTestEmail(@RequestParam String to) {

        try {
            Map<String, Object> context = new HashMap<>();
            context.put("email", to);
            context.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            log.info("Sending email to: {}", to);
            emailService.sendHtmlEmail(to, "Test: Password Changed", "password-changed", context);
            log.info("Email sent successfully to: {}", to);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Email sent to " + to);
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(500).body(response);
        }
    }
}
