package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.entity.VerificationToken;
import com.matiasugluck.deremate_backend.exception.BadRequestException;
import com.matiasugluck.deremate_backend.exception.NotFoundException;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
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

    public GenericResponseDTO verifyEmail(String token, String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("No user exists");
        }

        User user = optionalUser.get();

        if (user.isEmailVerified()) {
            throw new BadRequestException("Account is already verified");
        }

        VerificationToken verificationToken = tokenRepository.findByUser(user);
        if (!verificationToken.getToken().equals(token) || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid Token");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        return new GenericResponseDTO("Email verified");
    }

    public GenericResponseDTO resendVerification(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("No user exists");
        }

        User user = optionalUser.get();

        if (user.isEmailVerified()) {
            throw new BadRequestException("Account is already verified");
        }

        VerificationToken existingToken = tokenRepository.findByUser(user);
        tokenRepository.delete(existingToken);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);
        try {
            emailSender.sendVerificationEmail(user.getEmail(), token);
        } catch (MessagingException e) {
            throw new RuntimeException("Error while sending email", e);
        }
        return new GenericResponseDTO("Verification email sent");
    }
}
