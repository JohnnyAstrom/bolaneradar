package com.bolaneradar.backend.service.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Enhetstester för EmailService.
 *
 * Fokus:
 *  - Testa att korrekt e-post byggs och skickas via JavaMailSender
 *  - Testa felhantering utan att någon exception kastas vidare
 *  - Ingen riktig mailserver används – allt mockas
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    EmailService emailService;

    // ============================================================
    // TEST 1: skickar ett mail med korrekt innehåll
    // ============================================================
    @Test
    void sendErrorNotification_sendsEmailWithCorrectSubjectAndMessage() {
        // Arrange
        // Sätta privat fält via reflection eftersom @Value ersätts i runtime
        ReflectionTestUtils.setField(emailService, "toEmail", "test@example.com");

        String subject = "Scraper Error";
        String message = "Something went wrong";

        // Act
        emailService.sendErrorNotification(subject, message);

        // Assert
        // Fångar SimpleMailMessage-objektet som skickades
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage mail = captor.getValue();

        // Kontrollera att alla mailfält är korrekt satta
        assertArrayEquals(new String[]{"test@example.com"}, mail.getTo());
        assertEquals(subject, mail.getSubject());
        assertEquals(message, mail.getText());
    }

    // ============================================================
    // TEST 2: skickning misslyckas men kastar inte exception
    // ============================================================
    @Test
    void sendErrorNotification_doesNotThrow_whenMailSenderFails() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "toEmail", "test@example.com");

        // Simulera att mailSender.send() kastar exception
        doThrow(new RuntimeException("SMTP Down"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act + Assert
        // EmailService ska fånga exceptions internt → inget får kastas här
        assertDoesNotThrow(() ->
                emailService.sendErrorNotification("Subject", "Text")
        );
    }
}