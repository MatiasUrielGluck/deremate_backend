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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public GenericResponseDTO<String> sendPasswordResetToken(String email) {


        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {

            return new GenericResponseDTO<>(PasswordResetApiMessages.EMAIL_NOT_FOUND_OR_REQUEST_ERROR, HttpStatus.NOT_FOUND.value());
        }
        User user = optionalUser.get();

        List<VerificationToken> existingActiveTokens = tokenRepository.findAllByUserAndPurposeAndExpiryDateAfter(
                user, VerificationToken.PURPOSE_PASSWORD_RESET, LocalDateTime.now());
        if (!existingActiveTokens.isEmpty()) {
            tokenRepository.deleteAll(existingActiveTokens);
            logger.info("Invalidated {} existing active password reset token(s) for user {}", existingActiveTokens.size(), email);
        }

        String tokenValue = String.format("%04d", (int) (Math.random() * 10000));
        VerificationToken verificationToken = new VerificationToken(tokenValue, user); // Uses constructor with defaults

        tokenRepository.save(verificationToken);
        logger.info("Generated password reset token for user {}", email);

        try {
            // Ensure your email sender is robust and handles failures gracefully.
            emailSender.sendPasswordResetEmail(user.getEmail(), tokenValue);
            logger.info("Password reset email sent to {}", email);
        } catch (MessagingException e) {
            logger.error("Error sending password reset email to {}: {}", email, e.getMessage(), e);
            return new GenericResponseDTO<>(PasswordResetApiMessages.VERIFICATION_EMAIL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return new GenericResponseDTO<>(PasswordResetApiMessages.PASSWORD_RESET_TOKEN_SENT_SUCCESS, HttpStatus.OK.value());
    }

    @Transactional
    public GenericResponseDTO<String> resetPassword(PasswordResetRequestDto request) {


        Optional<VerificationToken> optToken = tokenRepository.findByTokenAndPurposeAndUser_EmailAndExpiryDateAfterAndAttemptCountLessThan(
                request.getToken(),
                VerificationToken.PURPOSE_PASSWORD_RESET,
                request.getEmail(),
                LocalDateTime.now(),
                VerificationToken.DEFAULT_MAX_ATTEMPTS
        );

        if (optToken.isEmpty()) {
            // Token not found, or expired, or max attempts already reached.
            // Log this failed attempt (IP, email, token tried if not sensitive).
            logger.warn("Invalid or expired password reset token attempt for email {} with token {}", request.getEmail(), request.getToken());
            // Optionally, you could check if a token exists but is *already* maxed out/expired to log differently
            // or to trigger account reset lockout sooner.
            return new GenericResponseDTO<>(PasswordResetApiMessages.TOKEN_INVALID_OR_EXPIRED, HttpStatus.UNAUTHORIZED.value());
        }

        VerificationToken verificationToken = optToken.get();
        User user = verificationToken.getUser(); // User is confirmed by the token query

        // Increment attempt count for this valid (but not yet successfully used) token.
        verificationToken.setAttemptCount(verificationToken.getAttemptCount() + 1);

        // Validate new password policy
        if (!isValidPassword(request.getPassword())) {
            if (verificationToken.isMaxAttemptsReached()) { // Check if this attempt maxed it out
                tokenRepository.delete(verificationToken);
                logger.warn("Password reset token for {} deleted after max attempts due to invalid password format.", request.getEmail());
            } else {
                tokenRepository.save(verificationToken); // Save incremented attempt count
            }
            return new GenericResponseDTO<>(PasswordResetApiMessages.PASSWORD_REQUIREMENTS, HttpStatus.BAD_REQUEST.value());
        }

        // If all checks pass, update password and invalidate token
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        try {
            userRepository.save(user);
            logger.info("Password successfully reset for user {}", user.getEmail());

            tokenRepository.delete(verificationToken); // CRITICAL: Token successfully used, delete it.
            logger.info("Password reset token for {} successfully used and deleted.", user.getEmail());

            // Send password change confirmation email (highly recommended)
            try {
                emailSender.sendPasswordChangedConfirmationEmail(user.getEmail());
            } catch (MessagingException e) {
                logger.error("Failed to send password changed confirmation email to {}: {}", user.getEmail(), e.getMessage(), e);
                // This is a secondary failure; the password WAS reset. Do not fail the whole operation.
            }

        } catch (Exception e) {
            logger.error("Error saving user or deleting token for user {}: {}", user.getEmail(), e.getMessage(), e);
            // If password save failed, the token attempt was still made.
            // The token was already updated with attemptCount or deleted if maxed.
            return new GenericResponseDTO<>(PasswordResetApiMessages.PASSWORD_SAVE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value());
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