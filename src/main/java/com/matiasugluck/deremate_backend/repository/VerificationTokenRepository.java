package com.matiasugluck.deremate_backend.repository;

import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Finds a specific token that is valid for use.
     * It checks the token string, purpose, ensures it's not expired, and that attempts are not maxed out.
     *
     * @param tokenValue The token string (4-digit code)
     * @param purpose The purpose of the token (e.g., VerificationToken.PURPOSE_PASSWORD_RESET)
     * @param email The email of the user associated with the token
     * @param now Current time, to check against expiryDate
     * @param maxAttempts The maximum allowed attempts for this token type
     * @return Optional of VerificationToken
     */
    Optional<VerificationToken> findByTokenAndPurposeAndUser_EmailAndExpiryDateAfterAndAttemptCountLessThan(
            String tokenValue, String purpose, String email, LocalDateTime now, int maxAttempts);

    /**
     * Finds all active (not expired) tokens for a specific user and purpose.
     * Used to invalidate old/other active tokens when a new one is requested for the same purpose.
     *
     * @param user The user entity
     * @param purpose The purpose of the tokens
     * @param now Current time, to check against expiryDate
     * @return List of active verification tokens
     */
    List<VerificationToken> findAllByUserAndPurposeAndExpiryDateAfter(
            User user, String purpose, LocalDateTime now);

    VerificationToken findByUser(User user);

    // Optional: To check if a user already has an active token before creating a new one (useful for rate limiting logic)
    // boolean existsByUserAndPurposeAndExpiryDateAfter(User user, String purpose, LocalDateTime now);

    // Deprecate or use with caution the generic finders for security-sensitive operations:
    // VerificationToken findByToken(String token); // Too generic, might fetch expired or wrong purpose
    // VerificationToken findByUser(User user); // Too generic
}