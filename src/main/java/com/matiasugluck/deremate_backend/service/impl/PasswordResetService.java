package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.PasswordResetRequestDto;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.entity.VerificationToken;
import com.matiasugluck.deremate_backend.exception.BadRequestException;
import com.matiasugluck.deremate_backend.exception.NotFoundException;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
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
    public GenericResponseDTO sendPasswordResetToken(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("No user exists with that email.");
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
        } catch (Exception e) {
            throw new BadRequestException("Error while sending verification email: " + e.getMessage());
        }


        return new GenericResponseDTO("Reset password token was succesfully sent.");
    }

    // Restablecer la contraseña
    public GenericResponseDTO resetPassword(PasswordResetRequestDto request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("No user exists with that email.");
        }

        User user = optionalUser.get();

        // Validar el token
        VerificationToken verificationToken = tokenRepository.findByUser(user);
        if (verificationToken == null || !verificationToken.getToken().equals(request.getToken()) || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid or expired token.");
        }

        // Validar la nueva contraseña
        if (!isValidPassword(request.getPassword())) {
            throw new BadRequestException("Password must contain at least one uppercase letter and one number.");
        }

        // Actualizar la contraseña del usuario
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new BadRequestException("Error while saving new password: " + e.getMessage());
        }

        return new GenericResponseDTO("Password reset succesfully.");
    }

    // Metodo para validar la contraseña
    private boolean isValidPassword(String password) {
        String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d).{6,}$";
        Pattern pattern = Pattern.compile(PASSWORD_REGEX);
        return pattern.matcher(password).matches();
    }
}
