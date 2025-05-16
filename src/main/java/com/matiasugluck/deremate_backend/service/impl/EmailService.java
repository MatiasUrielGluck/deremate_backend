package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.constants.EmailApiMessages;
import com.matiasugluck.deremate_backend.entity.VerificationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.ErrorManager;



@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("{spring.mail.username}")
    private String emailFrom;

    @Value("${spring.mail.password}")
    private String appPassword;

    @Autowired
    private JavaMailSender mailSender;

    private Session getEmailSession() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return Session.getInstance(prop, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailFrom, appPassword);
            }
        });
    }

    @Async
    public void sendVerificationEmail(String to, String code) throws MessagingException {
        if (to == null || to.isEmpty() || code == null || code.isEmpty()) {
            logger.error("Recipient email or code is null/empty. Cannot send verification email.");
            throw new IllegalArgumentException("Recipient email and code must not be null or empty.");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name()); // Specify UTF-8
            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(EmailApiMessages.EMAIL_VERIFICATION_SUBJECT); // Assuming a specific subject constant

            // Constructing a slightly more user-friendly body
            String emailBody = String.format(EmailApiMessages.EMAIL_VERIFICATION_BODY_TEMPLATE, code);
            helper.setText(emailBody, true); // true indicates HTML

            mailSender.send(message);
            logger.info("Verification email sent successfully to {}", to);
        } catch (MailException e) {
            logger.error("Failed to send verification email to {}: {}", to, e.getMessage(), e);
            throw new MessagingException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) throws MessagingException {
        if (toEmail == null || toEmail.isEmpty() || token == null || token.isEmpty()) {
            logger.error("Recipient email or token is null/empty. Cannot send password reset email.");
            throw new IllegalArgumentException("Recipient email and token must not be null or empty.");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            // Using MimeMessageHelper for easier multipart handling and encoding
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(emailFrom);
            helper.setTo(toEmail);
            helper.setSubject(EmailApiMessages.PASSWORD_RESET_SUBJECT);

            // Constructing the email body using a template from EmailApiMessages
            // This makes the message content configurable and cleaner.
            String emailBody = String.format(
                    EmailApiMessages.PASSWORD_RESET_BODY_TEMPLATE,
                    token,
                    VerificationToken.EXPIRY_MINUTES_PASSWORD_RESET // Using the constant from your entity
            );
            helper.setText(emailBody, true); // true indicates the body is HTML

            mailSender.send(message);
            logger.info("Password reset email sent successfully to {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
            throw new MessagingException("Failed to send password reset email: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a confirmation email to the user after their password has been successfully changed.
     *
     * @param toEmail The recipient's email address.
     * @throws MessagingException if there's an error during email sending.
     */
    @Async
    public void sendPasswordChangedConfirmationEmail(String toEmail) throws MessagingException {
        if (toEmail == null || toEmail.isEmpty()) {
            logger.error("Recipient email is null or empty. Cannot send password changed confirmation.");
            throw new IllegalArgumentException("Recipient email must not be null or empty.");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(emailFrom);
            helper.setTo(toEmail);
            helper.setSubject(EmailApiMessages.PASSWORD_CHANGED_CONFIRMATION_SUBJECT);

            // Using a predefined body from EmailApiMessages
            helper.setText(EmailApiMessages.PASSWORD_CHANGED_CONFIRMATION_BODY, true); // true indicates HTML

            mailSender.send(message);
            logger.info("Password changed confirmation email sent successfully to {}", toEmail);
        } catch (MailException e) {
            logger.error("Failed to send password changed confirmation email to {}: {}", toEmail, e.getMessage(), e);
            throw new MessagingException("Failed to send password changed confirmation email: " + e.getMessage(), e);
        }
    }
}
