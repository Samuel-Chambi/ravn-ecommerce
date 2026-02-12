package com.ravn.ecommerce.application.services;

import java.util.Map;

/**
 * Email service interface for sending HTML emails using templates.
 */
public interface EmailService {

    /**
     * Sends an HTML email
     * Executed asynchronously to avoid blocking the main thread.
     */
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> context);
}
