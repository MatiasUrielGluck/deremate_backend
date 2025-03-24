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
            throw new NotFoundException("No existe un usuario con ese email");
        }

        User user = optionalUser.get();

        if (user.isEmailVerified()) {
            throw new BadRequestException("La cuenta ya fue verificada");
        }

        VerificationToken verificationToken = tokenRepository.findByUser(user);
        if (!verificationToken.getToken().equals(token) || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token inválido");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        return new GenericResponseDTO("Email verficiado exitosamente");
    }

    public GenericResponseDTO resendVerification(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("No existe un usuario con ese email");
        }

        User user = optionalUser.get();

        if (user.isEmailVerified()) {
            throw new BadRequestException("La cuenta ya fue verificada");
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
            throw new RuntimeException("Error al enviar el correo de verificación", e);
        }
        return new GenericResponseDTO("Correo de verificación enviado exitosamente.");
    }
}
