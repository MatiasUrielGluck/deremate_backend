package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.constants.PasswordResetApiMessages;
import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.PasswordResetRequestDto;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.entity.VerificationToken;
import com.matiasugluck.deremate_backend.exception.BadRequestException;
import com.matiasugluck.deremate_backend.exception.NotFoundException;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailSender;
    private final PasswordEncoder passwordEncoder;

    // Enviar el token para restablecer la contraseña
    public GenericResponseDTO<String> sendPasswordResetToken(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return new GenericResponseDTO<>(PasswordResetApiMessages.EMAIL_NOT_FOUND, HttpStatus.NOT_FOUND.value());
        }
        User user = optionalUser.get();

        // Verificar y eliminar cualquier token existente
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
            emailSender.sendPasswordResetEmail(user.getEmail(), token);
        } catch (MessagingException e) {
            return new GenericResponseDTO<>(PasswordResetApiMessages.VERIFICATION_EMAIL_ERROR + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return new GenericResponseDTO<>(PasswordResetApiMessages.PASSWORD_RESET_SUCCESS, HttpStatus.OK.value());
    }

    // Restablecer la contraseña
    public GenericResponseDTO<String> resetPassword(PasswordResetRequestDto request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return new GenericResponseDTO<>(PasswordResetApiMessages.EMAIL_NOT_FOUND, HttpStatus.NOT_FOUND.value());
        }

        User user = optionalUser.get();

        // Validar el token
        VerificationToken verificationToken = tokenRepository.findByUser(user);
        if (verificationToken == null || !verificationToken.getToken().equals(request.getToken()) || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new GenericResponseDTO<>(PasswordResetApiMessages.TOKEN_INVALID_OR_EXPIRED, HttpStatus.UNAUTHORIZED.value());
        }

        // Validar la nueva contraseña
        if (!isValidPassword(request.getPassword())) {
            return new GenericResponseDTO<>(PasswordResetApiMessages.PASSWORD_REQUIREMENTS, HttpStatus.BAD_REQUEST.value());
        }

        // Actualizar la contraseña del usuario
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        try {
            userRepository.save(user);
        } catch (Exception e) {
            return new GenericResponseDTO<>(PasswordResetApiMessages.PASSWORD_SAVE_ERROR + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return new GenericResponseDTO<>(PasswordResetApiMessages.PASSWORD_RESET_SUCCESSFULLY, HttpStatus.OK.value());
    }

    // Metodo para validar la contraseña
    private boolean isValidPassword(String password) {
        String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d).{6,}$";
        Pattern pattern = Pattern.compile(PASSWORD_REGEX);
        return pattern.matcher(password).matches();
    }
}