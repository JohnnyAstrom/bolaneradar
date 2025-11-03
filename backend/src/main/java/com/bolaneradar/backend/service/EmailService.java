package com.bolaneradar.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Hanterar e-postutskick vid fel eller notifieringar.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${alert.email.to}")
    private String toEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Skickar ett e-postmeddelande med felinformation.
     *
     * @param subject Rubrik för meddelandet
     * @param message Innehåll i mejlet
     */
    public void sendErrorNotification(String subject, String message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(toEmail);
            mail.setSubject(subject);
            mail.setText(message);
            mailSender.send(mail);
            System.out.println("Felrapport skickad till " + toEmail);
        } catch (Exception e) {
            System.err.println("Kunde inte skicka e-post: " + e.getMessage());
        }
    }
}