package com.matiasugluck.deremate_backend.constants;

public class AuthApiMessages {
    public static final String NOT_EXISTING_USER = "No user exists with the provided email.";
    public static final String EMAIL_NOT_VERIFIED = "The email has not been verified.";
    public static final String INVALID_CREDENTIALS = "Invalid credentials.";
    public static final String USER_DISABLED = "User is disabled.";
    public static final String LOGIN_SUCCESSFUL = "Login successful.";
    public static final String ERROR_SENDING_VERIFICATION_EMAIL = "Error sending verification email: ";
    public static final String USER_REGISTERED_SUCCESSFULLY = "User registered successfully. A verification email has been sent to ";
    public static final String ALREADY_EXISTING_EMAIL = "This email address is already registered. Please try logging in or use a different email.";
    public static final String USER_REGISTRATION_ERROR = "An unexpected error occurred during user registration. Please try again later.";
    public static final String USER_REGISTRATION_ERROR_TOKEN_GENERATION = "User registered, but an error occurred while generating the verification token. Please try resending the verification email.";
    public static final String USER_REGISTERED_SUCCESSFULLY_VERIFICATION_SENT = "User registered successfully! A verification email has been sent to: "; // Append email address
    public static final String USER_REGISTERED_SUCCESSFULLY_EMAIL_FAILED = "User registered successfully, but we encountered an issue sending the verification email. Please try the 'resend verification' option or contact support. Registered email: "; // Append email address
}



























