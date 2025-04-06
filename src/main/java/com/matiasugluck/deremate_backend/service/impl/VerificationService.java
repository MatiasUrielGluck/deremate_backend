package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.constants.VerificationApiMessages;
import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.entity.VerificationToken;
import com.matiasugluck.deremate_backend.exception.BadRequestException;
import com.matiasugluck.deremate_backend.exception.NotFoundException;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailSender;

    public GenericResponseDTO<String> verifyEmail(String token, String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return new GenericResponseDTO<>(VerificationApiMessages.NOT_EXISTING_USER, HttpStatus.NOT_FOUND.value());
        }

        User user = optionalUser.get();

        if (user.isEmailVerified()) {
            return new GenericResponseDTO<>(VerificationApiMessages.EMAIL_ALREADY_VERIFIED, HttpStatus.BAD_REQUEST.value());
        }

        VerificationToken verificationToken = tokenRepository.findByUser(user);
        if (verificationToken == null || !verificationToken.getToken().equals(token) || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new GenericResponseDTO<>(VerificationApiMessages.INVALID_TOKEN, HttpStatus.BAD_REQUEST.value());
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        return new GenericResponseDTO<>(VerificationApiMessages.EMAIL_VERIFIED, HttpStatus.OK.value());
    }

    public GenericResponseDTO<String> resendVerification(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return new GenericResponseDTO<>(VerificationApiMessages.NOT_EXISTING_USER, HttpStatus.NOT_FOUND.value());
        }

        User user = optionalUser.get();

        if (user.isEmailVerified()) {
            return new GenericResponseDTO<>(VerificationApiMessages.EMAIL_ALREADY_VERIFIED, HttpStatus.BAD_REQUEST.value());
        }

        VerificationToken existingToken = tokenRepository.findByUser(user);
        if (existingToken != null) {
            tokenRepository.delete(existingToken);
        }

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);
        try {
            emailSender.sendVerificationEmail(user.getEmail(), token);
        } catch (MessagingException e) {
            return new GenericResponseDTO<>(VerificationApiMessages.ERROR_SENDING_EMAIL, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return new GenericResponseDTO<>(VerificationApiMessages.VERIFICATION_EMAIL_SENT, HttpStatus.OK.value());
    }
}