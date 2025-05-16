package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.constants.VerificationApiMessages;
import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.entity.VerificationToken;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.repository.VerificationTokenRepository;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // This handles constructor injection for final fields
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    // Ensure this 'EmailSender' matches the class name of the service provided in the Canvas
    private final EmailService emailSender; // Corrected type to EmailSender

    @Transactional
    public GenericResponseDTO<String> verifyEmail(String tokenValue, String email) {
        // Rate limiting per IP/email for verification attempts should be considered here or via a filter

        Optional<VerificationToken> optToken = tokenRepository.findByTokenAndPurposeAndUser_EmailAndExpiryDateAfterAndAttemptCountLessThan(
                tokenValue,
                VerificationToken.PURPOSE_EMAIL_VERIFICATION,
                email,
                LocalDateTime.now(),
                VerificationToken.MAX_ATTEMPTS_EMAIL_VERIFICATION // Use constant for max attempts
        );

        if (optToken.isEmpty()) {
            logger.warn("Invalid, expired, or max attempts email verification token attempt for email {} with token {}", email, tokenValue);
            // To prevent user enumeration, you might want to check if a token exists but is just expired/maxed
            // and log that internally, but return a generic error to the user.
            return new GenericResponseDTO<>(VerificationApiMessages.INVALID_TOKEN, HttpStatus.BAD_REQUEST.value());
        }

        VerificationToken verificationToken = optToken.get();
        User user = verificationToken.getUser(); // User is confirmed by the token query

        if (user.isEmailVerified()) {

            tokenRepository.delete(verificationToken);
            logger.info("User {} email already verified. Deleting token.", email);
            return new GenericResponseDTO<>(VerificationApiMessages.EMAIL_ALREADY_VERIFIED, HttpStatus.BAD_REQUEST.value());
        }

        // Increment attempt count for this specific token instance.
        // This happens regardless of whether the token string itself was the final point of validation,
        // as an attempt was made to use it.
        verificationToken.setAttemptCount(verificationToken.getAttemptCount() + 1);

        // The query already validated the token string, purpose, email, expiry, and attempt count.
        // So, if we are here, the token is correct and valid for use.
        user.setEmailVerified(true);
        userRepository.save(user);
        logger.info("Email successfully verified for user {}", email);

        tokenRepository.delete(verificationToken); // CRITICAL: Token successfully used, delete it.
        logger.info("Email verification token for {} successfully used and deleted.", email);

        return new GenericResponseDTO<>(VerificationApiMessages.EMAIL_VERIFIED, HttpStatus.OK.value());
    }

    @Transactional
    public GenericResponseDTO<String> resendVerification(String email) {
        // Rate limiting per IP/email for resend requests is crucial here

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            logger.warn("Attempt to resend verification for non-existing user: {}", email);
            return new GenericResponseDTO<>(VerificationApiMessages.NOT_EXISTING_USER, HttpStatus.NOT_FOUND.value());
        }

        User user = optionalUser.get();

        if (user.isEmailVerified()) {
            logger.info("Attempt to resend verification for already verified user: {}", email);
            return new GenericResponseDTO<>(VerificationApiMessages.EMAIL_ALREADY_VERIFIED, HttpStatus.BAD_REQUEST.value());
        }

        // Invalidate any existing *active* email verification tokens for this user.
        List<VerificationToken> existingActiveTokens = tokenRepository.findAllByUserAndPurposeAndExpiryDateAfter(
                user, VerificationToken.PURPOSE_EMAIL_VERIFICATION, LocalDateTime.now());
        if (!existingActiveTokens.isEmpty()) {
            tokenRepository.deleteAll(existingActiveTokens);
            logger.info("Invalidated {} existing active email verification token(s) for user {}", existingActiveTokens.size(), email);
        }

        String tokenValue = String.format("%04d", (int) (Math.random() * 10000)); // 4-digit token

        // Using the assumed constructor from VerificationToken entity
        VerificationToken newVerificationToken = new VerificationToken(
                tokenValue,
                user,
                VerificationToken.PURPOSE_EMAIL_VERIFICATION,
                VerificationToken.EXPIRY_HOURS_EMAIL_VERIFICATION // Use constant for expiry
        );
        // The constructor should set attemptCount to 0 by default.

        tokenRepository.save(newVerificationToken);
        logger.info("Generated new email verification token for user {}", email);

        try {
            emailSender.sendVerificationEmail(user.getEmail(), tokenValue);
            logger.info("Verification email resent successfully to {}", email);
        } catch (MessagingException e) {
            logger.error("Error resending verification email to {}: {}", email, e.getMessage(), e);
            return new GenericResponseDTO<>(VerificationApiMessages.ERROR_SENDING_EMAIL, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return new GenericResponseDTO<>(VerificationApiMessages.VERIFICATION_EMAIL_SENT, HttpStatus.OK.value());
    }
}
