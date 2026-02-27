package com.ravn.ecommerce.infrastructure.email;

import com.ravn.ecommerce.application.config.AppConfig;
import com.resend.Resend;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private EmailTemplateEngine emailTemplateEngine;
    @Mock
    private AppConfig appConfig;
    @Mock
    private AppConfig.EmailConfig emailConfig;
    @Mock
    private AppConfig.EmailConfig.ResendConfig resendConfig;
    @Mock
    private Resend resend;
    @Mock
    private Emails emails;

    private ResendEmailServiceImpl emailService;

    /**
     * Since the constructor creates a new Resend internally,
     * we build the service with valid config and then inject a mock Resend via reflection.
     */
    @BeforeEach
    void setUp() throws Exception {
        when(appConfig.getEmail()).thenReturn(emailConfig);
        when(emailConfig.getResend()).thenReturn(resendConfig);
        when(resendConfig.getApiKey()).thenReturn("re_test_123456");

        emailService = new ResendEmailServiceImpl(emailTemplateEngine, appConfig);

        // Replace the real Resend with a mock via reflection
        Field resendField = ResendEmailServiceImpl.class.getDeclaredField("resend");
        resendField.setAccessible(true);
        resendField.set(emailService, resend);
    }

    @Test
    @DisplayName("Should send email via Resend API when enabled")
    void shouldSendEmailViaResend() throws Exception {
        when(emailConfig.isEnabled()).thenReturn(true);
        when(emailConfig.getFrom()).thenReturn("noreply@test.com");
        when(emailTemplateEngine.process(anyString(), anyMap())).thenReturn("<h1>Hello</h1>");
        when(resend.emails()).thenReturn(emails);
        when(emails.send(any(CreateEmailOptions.class)))
                .thenReturn(new CreateEmailResponse("email-id-123"));

        emailService.sendHtmlEmail("user@test.com", "Welcome", "welcome", Map.of("name", "John"));

        verify(resend.emails()).send(any(CreateEmailOptions.class));
        verify(emailTemplateEngine).process("welcome", Map.of("name", "John"));
    }

    @Test
    @DisplayName("Should skip sending when email service is disabled")
    void shouldSkipWhenDisabled() {
        when(emailConfig.isEnabled()).thenReturn(false);

        emailService.sendHtmlEmail("user@test.com", "Subject", "template", Map.of());

        verifyNoInteractions(resend);
        verifyNoInteractions(emailTemplateEngine);
    }

    @Test
    @DisplayName("Should throw when Resend API key is missing")
    void shouldThrowWhenApiKeyMissing() {
        AppConfig config = mock(AppConfig.class);
        AppConfig.EmailConfig emailCfg = mock(AppConfig.EmailConfig.class);
        AppConfig.EmailConfig.ResendConfig resendCfg = mock(AppConfig.EmailConfig.ResendConfig.class);

        when(config.getEmail()).thenReturn(emailCfg);
        when(emailCfg.getResend()).thenReturn(resendCfg);
        when(resendCfg.getApiKey()).thenReturn("");

        assertThatThrownBy(() -> new ResendEmailServiceImpl(emailTemplateEngine, config))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Resend API key is missing");
    }
}
