package com.matiasugluck.deremate_backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
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

    public void sendVerificationEmail(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(emailFrom);
        helper.setTo(to);
        helper.setSubject("Email Verification");
        helper.setText("Your verification code is: " + code, true);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(emailFrom);
        helper.setTo(to);
        helper.setSubject("Password Reset");
        helper.setText("Your password reset token is: " + token, true);
        mailSender.send(message);
    }
}