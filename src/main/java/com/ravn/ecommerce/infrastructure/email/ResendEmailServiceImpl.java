package com.ravn.ecommerce.infrastructure.email;

import com.ravn.ecommerce.application.config.AppConfig;
import com.ravn.ecommerce.application.services.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.email.provider", havingValue = "resend")
public class ResendEmailServiceImpl implements EmailService {

    private final EmailTemplateEngine emailTemplateEngine;
    private final AppConfig appConfig;
    private final Resend resend;

    public ResendEmailServiceImpl(EmailTemplateEngine emailTemplateEngine, AppConfig appConfig) {
        this.emailTemplateEngine = emailTemplateEngine;
        this.appConfig = appConfig;
        // Setting apikey
        String apiKey = appConfig.getEmail().getResend() != null
                ? appConfig.getEmail().getResend().getApiKey()
                : null;

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Resend API key is missing. Please set RESEND_API_KEY.");
        }

        this.resend = new Resend(apiKey);
        log.info("Initialized ResendEmailServiceImpl (Provider: Resend)");
    }

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> context) {
        if (!appConfig.getEmail().isEnabled()) {
            log.info("Email service is disabled. Skipping sending email '{}' to {}", subject, to);
            return;
        }

        try {
            // Thymeleaf template
            String htmlContent = emailTemplateEngine.process(templateName, context);

            CreateEmailOptions sendEmailRequest = CreateEmailOptions.builder()
                    .from(appConfig.getEmail().getFrom())
                    .to(to)
                    .subject(subject)
                    .html(htmlContent)
                    .build();
            CreateEmailResponse data = resend.emails().send(sendEmailRequest);
            log.info("Email '{}' sent to {} via Resend API (ID: {})", subject, to, data.getId());

        } catch (ResendException e) {
            log.error("Failed to send email to {} via Resend API", to, e);
            throw new RuntimeException("Failed to send email via Resend", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}", to, e);
            throw new RuntimeException("Unexpected error sending email", e);
        }
    }
}
