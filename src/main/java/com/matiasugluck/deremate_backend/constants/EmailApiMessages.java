package com.matiasugluck.deremate_backend.constants;

public class EmailApiMessages {
    public static final String EMAIL_VERIFICATION = "Email Verification";
    public static final String YOUR_VERIFICATION_CODE_IS = "Your verification code is: ";
    public static final String PASSWORD_RESET = "Password Reset";
    public static final String YOUR_PASSWORD_RESET_TOKEN_IS = "Your password reset token is: ";
    public static final String EMAIL_VERIFICATION_SUBJECT = "Verify Your Email Address";
    public static final String EMAIL_VERIFICATION_BODY_TEMPLATE = "Hello,<br><br>Thank you for registering. Your verification code is: <strong>%s</strong><br><br>Please use this code to complete your registration.<br><br>Thanks,<br>The Team";

    // Password Reset
    public static final String PASSWORD_RESET_SUBJECT = "Password Reset Request";
    public static final String PASSWORD_RESET_BODY_TEMPLATE = "Hello,<br><br>" +
            "You requested a password reset for your account.<br>" +
            "Your password reset code is: <strong>%s</strong><br><br>" +
            "This code will expire in %d minutes.<br><br>" +
            "If you did not request this, please ignore this email.<br><br>" +
            "Thanks,<br>DeRemate Logistic Team";

    // Password Changed Confirmation
    public static final String PASSWORD_CHANGED_CONFIRMATION_SUBJECT = "Your Password Has Been Changed";
    public static final String PASSWORD_CHANGED_CONFIRMATION_BODY = "Hello,<br><br>" +
            "This email confirms that the password for your account has been successfully changed.<br><br>" +
            "If you did not authorize this change, please contact our support team immediately or try to secure your account.<br><br>" +
            "Thanks,<br>The Team";
}
