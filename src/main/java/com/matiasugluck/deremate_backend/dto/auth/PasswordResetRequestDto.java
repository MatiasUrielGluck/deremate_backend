package com.matiasugluck.deremate_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetRequestDto {

    // Getters and Setters
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Token cannot be blank")
    private String token; // The 4-digit code

    @NotBlank(message = "Password cannot be blank")
    // Add your password complexity annotations here if not handled by isValidPassword only
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

}
