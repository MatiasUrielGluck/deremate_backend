package com.matiasugluck.deremate_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
public class VerificationToken {

    public static final int DEFAULT_MAX_ATTEMPTS = 3; // Max attempts for guessing this specific token instance
    public static final String PURPOSE_PASSWORD_RESET = "PASSWORD_RESET";
    public static final int EXPIRY_MINUTES_PASSWORD_RESET = 5; // Very short expiry
    public static final String PURPOSE_EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    public static final int EXPIRY_HOURS_EMAIL_VERIFICATION = 24; // Or a shorter duration like 1 hour
    public static final int MAX_ATTEMPTS_EMAIL_VERIFICATION = 5; // Max attempts for this specific token

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token; // The 4-digit code

    @ManyToOne(optional = false, fetch = FetchType.LAZY) // A user can have multiple tokens over time
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private String purpose; // e.g., "PASSWORD_RESET", "EMAIL_VERIFICATION"

    @Column(nullable = false)
    private int attemptCount = 0; // Number of times this specific token instance was tried

    public VerificationToken(String token, User user, String purpose, int expiryMinutes, int initialAttemptCount) {
        this.token = token;
        this.user = user;
        this.purpose = purpose;
        this.expiryDate = LocalDateTime.now().plusMinutes(expiryMinutes);
        this.attemptCount = initialAttemptCount;
    }

    // Adjust constructor or add a new one for email verification tokens
    public VerificationToken(String token, User user, String purpose, int expiryHours) {
        this.token = token;
        this.user = user;
        this.purpose = purpose;
        this.expiryDate = LocalDateTime.now().plusHours(expiryHours);
        this.attemptCount = 0; // Initialize attempt count
    }

    // Convenience constructor for password reset
    public VerificationToken(String token, User user) {
        this(token, user, PURPOSE_PASSWORD_RESET, EXPIRY_MINUTES_PASSWORD_RESET, 0);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    public boolean isMaxAttemptsReached() {
        return this.attemptCount >= DEFAULT_MAX_ATTEMPTS;
    }
}