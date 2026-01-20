package com.bolaneradar.backend.service.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * ================================================================
 * EMAIL SERVICE
 * ================================================================
 * Ansvarar för utskick av notifieringar via e-post.
 * <p>
 * Användning:
 * - Anropas främst vid fel under scraping
 * - Skickar enkla textbaserade felmeddelanden
 * <p>
 * Konfiguration:
 * - Aktiveras endast om `app.email.enabled=true`
 * - Mottagare styrs via `alert.email.to`
 * <p>
 * Design:
 * - Valfri integration (ConditionalOnProperty)
 * - Ingen affärslogik, endast notifiering
 * ================================================================
 */
@Service
@ConditionalOnProperty(
        name = "app.email.enabled",
        havingValue = "true"
)
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${alert.email.to}")
    private String toEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an email notification describing the error.
     */
    public void sendErrorNotification(String subject, String message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(toEmail);
            mail.setSubject(subject);
            mail.setText(message);
            mailSender.send(mail);
            System.out.println("Error report sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("Could not send email: " + e.getMessage());
        }
    }
}