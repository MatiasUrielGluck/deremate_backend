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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
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

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            return new GenericResponseDTO<>(AuthApiMessages.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED.value());
        } catch (DisabledException e) {
            return new GenericResponseDTO<>(AuthApiMessages.USER_DISABLED, HttpStatus.UNAUTHORIZED.value());
        }

        if (!user.isEmailVerified()) {
            return new GenericResponseDTO<>(AuthApiMessages.EMAIL_NOT_VERIFIED, HttpStatus.UNAUTHORIZED.value());
        }

        // Generar token para el usuario autenticado
        String jwtToken = jwtService.generateTokenWithUserId(user,user.getId());

        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return new GenericResponseDTO<>(loginResponseDTO, AuthApiMessages.LOGIN_SUCCESSFUL, HttpStatus.OK.value());
    }

    @Transactional
    public GenericResponseDTO<Void> signup(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            logger.warn("Signup attempt with already existing email: {}", email);
            return new GenericResponseDTO<>(AuthApiMessages.ALREADY_EXISTING_EMAIL, HttpStatus.CONFLICT.value()); // Using 409 Conflict
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstname(firstName)
                .lastname(lastName)
                .isEmailVerified(false) // Explicitly set to false on signup
                // Set other defaults like roles, enabled status if applicable
                .build();

        try {
            userRepository.save(user);
            logger.info("New user saved with email: {}", email);
        } catch (Exception e) {
            logger.error("Error saving new user {}: {}", email, e.getMessage(), e);
            return new GenericResponseDTO<>(AuthApiMessages.USER_REGISTRATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        // Invalidate any previous active email verification tokens for this user.
        // This is a safeguard, less likely for a brand new user unless a previous attempt failed mid-way.
        List<VerificationToken> existingActiveTokens = tokenRepository.findAllByUserAndPurposeAndExpiryDateAfter(
                user, VerificationToken.PURPOSE_EMAIL_VERIFICATION, LocalDateTime.now());
        if (!existingActiveTokens.isEmpty()) {
            tokenRepository.deleteAll(existingActiveTokens);
            logger.info("Invalidated {} existing active email verification token(s) for new user {}", existingActiveTokens.size(), email);
        }

        // Generar código de verificación de 4 dígitos
        String tokenValue = String.format("%04d", (int) (Math.random() * 10000));

        VerificationToken verificationToken = new VerificationToken(
                tokenValue,
                user,
                VerificationToken.PURPOSE_EMAIL_VERIFICATION,
                VerificationToken.EXPIRY_HOURS_EMAIL_VERIFICATION // Use constant for expiry
        );
        // The constructor should set attemptCount to 0 by default.

        try {
            tokenRepository.save(verificationToken);
            logger.info("Verification token generated and saved for user {}", email);
        } catch (Exception e) {
            logger.error("Error saving verification token for user {}: {}", email, e.getMessage(), e);
            // If token saving fails, the user is created but can't verify.
            // This might require a manual admin intervention or a robust "resend verification" flow.
            // For now, returning a generic registration error.
            return new GenericResponseDTO<>(AuthApiMessages.USER_REGISTRATION_ERROR_TOKEN_GENERATION, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }


        try {
            emailSender.sendVerificationEmail(user.getEmail(), tokenValue);
            logger.info("Verification email sent to new user {}", email);
        } catch (MessagingException e) {
            logger.error("Error sending verification email to new user {}: {}", email, e.getMessage(), e);
            // User is created, token is created, but email failed.
            // The user can use "resend verification" later.
            // Inform the user that registration was successful but email sending failed.
            return new GenericResponseDTO<>(
                    AuthApiMessages.USER_REGISTERED_SUCCESSFULLY_EMAIL_FAILED + user.getEmail(),
                    HttpStatus.CREATED.value() // Still CREATED, but with a warning.
            );
        }

        return new GenericResponseDTO<>(
                AuthApiMessages.USER_REGISTERED_SUCCESSFULLY_VERIFICATION_SENT + user.getEmail(),
                HttpStatus.CREATED.value()
        );
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
