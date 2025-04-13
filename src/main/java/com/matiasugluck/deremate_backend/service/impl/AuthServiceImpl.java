package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.constants.AuthApiMessages;
import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.LoginResponseDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.entity.VerificationToken;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.repository.VerificationTokenRepository;
import com.matiasugluck.deremate_backend.service.AuthService;
import com.matiasugluck.deremate_backend.service.JwtService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailSender;

    @Override
    public GenericResponseDTO<Object> login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return new GenericResponseDTO<>(AuthApiMessages.NOT_EXISTING_USER, HttpStatus.NOT_FOUND.value());
        }

        User user = optionalUser.get();

        if (!user.isEmailVerified()) {
            return new GenericResponseDTO<>(AuthApiMessages.EMAIL_NOT_VERIFIED, HttpStatus.UNAUTHORIZED.value());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            return new GenericResponseDTO<>(AuthApiMessages.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED.value());
        } catch (DisabledException e) {
            return new GenericResponseDTO<>(AuthApiMessages.USER_DISABLED, HttpStatus.UNAUTHORIZED.value());
        }

        // Generar token para el usuario autenticado
        String jwtToken = jwtService.generateToken(user);

        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return new GenericResponseDTO<>(loginResponseDTO, AuthApiMessages.LOGIN_SUCCESSFUL, HttpStatus.OK.value());
    }

    @Override
    public GenericResponseDTO<Void> signup(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            return new GenericResponseDTO<>(AuthApiMessages.ALREADY_EXISTING_EMAIL, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstname(firstName)
                .lastname(lastName)
                .build();

        userRepository.save(user);

        // Generar código de verificación de 4 dígitos
        String token = String.format("%04d", (int)(Math.random() * 10000));

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);

        try {
            emailSender.sendVerificationEmail(user.getEmail(), token);
        } catch (MessagingException e) {
            return new GenericResponseDTO<>(
                    AuthApiMessages.ERROR_SENDING_VERIFICATION_EMAIL + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

        return new GenericResponseDTO<>(
                AuthApiMessages.USER_REGISTERED_SUCCESSFULLY + user.getEmail(),
                HttpStatus.CREATED.value()
        );
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
