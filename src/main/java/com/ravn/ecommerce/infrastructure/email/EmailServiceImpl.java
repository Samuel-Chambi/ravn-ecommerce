package com.ravn.ecommerce.infrastructure.email;

import com.ravn.ecommerce.application.config.AppConfig;
import com.ravn.ecommerce.application.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Email service implementation using JavaMail and Thymeleaf templates.
 * Uses @Async to send emails in a separate thread for better performance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailTemplateEngine emailTemplateEngine;
    private final AppConfig appConfig;

    @Override
    @Async // Sends email in separate thread to avoid blocking
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> context) {
        if (!appConfig.getEmail().isEnabled()) {
            log.info("Email service is disabled. Skipping sending email '{}' to {}", subject, to);
            return;
        }

        try {
            // Process Thymeleaf template with context variables
            String htmlContent = emailTemplateEngine.process(templateName, context);

            // Create MIME message
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configure email
            helper.setFrom(appConfig.getEmail().getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content

            // Send via SMTP
            javaMailSender.send(message);
            log.info("Email '{}' sent to {}", subject, to);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
