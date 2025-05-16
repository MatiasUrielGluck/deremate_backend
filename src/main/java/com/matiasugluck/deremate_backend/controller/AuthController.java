package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.*;
import com.matiasugluck.deremate_backend.service.AuthService;
import com.matiasugluck.deremate_backend.service.impl.PasswordResetService;
import com.matiasugluck.deremate_backend.service.impl.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "${base-path-v1}/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and management")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final VerificationService verificationService;

    // LOGIN
    @Operation(summary = "Authenticate user and return JWT token")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials, user disabled, email not verified", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/login")
    public ResponseEntity<GenericResponseDTO<Object>> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        GenericResponseDTO<Object> response = authService.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // SIGNUP
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Email already exists or error sending verification email", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/signup")
    public ResponseEntity<GenericResponseDTO<String>> signup(@RequestBody @Valid SignupRequestDTO signupRequestDTO) {
        GenericResponseDTO<String> response = authService.signup(
                signupRequestDTO.getEmail(),
                signupRequestDTO.getPassword(),
                signupRequestDTO.getFirstName(),
                signupRequestDTO.getLastName());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // FORGOT PASSWORD
    @Operation(summary = "Request a password reset token (4-digit code via email)")
    @ApiResponse(responseCode = "200", description = "Password reset token instructions sent", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request (e.g., malformed email)", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found or other request issue", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))) // For rate limiting
    @ApiResponse(responseCode = "500", description = "Error while processing request or sending email", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/forgot-password")
    public ResponseEntity<GenericResponseDTO<String>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequestDto forgotPasswordRequestDto) {
        GenericResponseDTO<String> response = passwordResetService.sendPasswordResetToken(forgotPasswordRequestDto.getEmail());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // RESET PASSWORD
    @Operation(summary = "Reset the user's password using the 4-digit code and new password")
    @ApiResponse(responseCode = "200", description = "Password reset successfully", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request (e.g., password format, missing fields)", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Invalid, expired, or maxed attempts for token", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "429", description = "Too many attempts", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))) // For rate limiting
    @ApiResponse(responseCode = "500", description = "Error while resetting password", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/reset-password")
    public ResponseEntity<GenericResponseDTO<String>> resetPassword(@RequestBody @Valid PasswordResetRequestDto request) {
        GenericResponseDTO<String> response = passwordResetService.resetPassword(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // VERIFY EMAIL
    @Operation(summary = "Verify user's email using token and email")
    @ApiResponse(responseCode = "200", description = "Email verified successfully", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request (e.g., invalid token, email format, or account already verified)", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    // 404 for "User not found" might be covered by the service logic if the token/email doesn't match an existing user or token record.
    // The service might return a 400/401 for "invalid token" in such cases to avoid user enumeration.
    // Adjust based on your VerificationService's response for non-existent users during token validation.
    @ApiResponse(responseCode = "401", description = "Invalid or expired token", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/verify") // Changed endpoint to be more descriptive, e.g., /verify-email
    public ResponseEntity<GenericResponseDTO<String>> verifyEmail(
            @RequestBody @Valid EmailVerificationRequestDto requestDto) {
        // Call the service method, passing the token and email from the DTO
        GenericResponseDTO<String> response = verificationService.verifyEmail(requestDto.getToken(), requestDto.getEmail());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // RESEND VERIFICATION EMAIL
    @Operation(summary = "Resend the email verification token")
    @ApiResponse(responseCode = "200", description = "Verification email sent successfully", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request (e.g., email format, or account already verified)", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Error while sending the email or processing request", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/resend-verification") // Endpoint name updated for clarity
    public ResponseEntity<GenericResponseDTO<String>> resendVerification(
            @RequestBody @Valid ResendVerificationRequestDto requestDto) { // Changed to use RequestBody and ResendVerificationRequestDto
        GenericResponseDTO<String> response = verificationService.resendVerification(requestDto.getEmail());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
