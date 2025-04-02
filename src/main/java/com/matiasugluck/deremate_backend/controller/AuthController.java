package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.LoginRequestDTO;
import com.matiasugluck.deremate_backend.dto.auth.LoginResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.PasswordResetRequestDto;
import com.matiasugluck.deremate_backend.dto.auth.SignupRequestDTO;
import com.matiasugluck.deremate_backend.service.AuthService;
import com.matiasugluck.deremate_backend.service.impl.PasswordResetService;
import com.matiasugluck.deremate_backend.service.impl.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "${base-path-v1}/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and management")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final VerificationService verificationService;

    @Operation(summary = "Authenticate user and return JWT token")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials, user disabled, email not verified", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/login")
    public ResponseEntity<GenericResponseDTO<Object>> login(@RequestBody @Validated LoginRequestDTO loginRequestDTO) {
        GenericResponseDTO<Object> response = authService.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Email already exists or error sending verification email", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/signup")
    public ResponseEntity<GenericResponseDTO<String>> signup(@RequestBody @Validated SignupRequestDTO signupRequestDTO) {
        GenericResponseDTO<String> response = authService.signup(signupRequestDTO.getEmail(), signupRequestDTO.getPassword());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Request a password reset token")
    @ApiResponse(responseCode = "200", description = "Password reset token sent", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Error while sending verification email", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/forgot-password")
    public ResponseEntity<GenericResponseDTO<String>> forgotPassword(@RequestParam("email") @Parameter(description = "User's email address", required = true) String email) {
        GenericResponseDTO<String> response = passwordResetService.sendPasswordResetToken(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Reset the user's password")
    @ApiResponse(responseCode = "200", description = "Password reset successfully", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid password format", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Invalid or expired token", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/reset-password")
    public ResponseEntity<GenericResponseDTO<String>> resetPassword(@RequestBody PasswordResetRequestDto request) {
        GenericResponseDTO<String> response = passwordResetService.resetPassword(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Verify user's email")
    @ApiResponse(responseCode = "200", description = "Email verified successfully", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid token or account already verified", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/verify")
    public ResponseEntity<GenericResponseDTO<String>> verifyEmail(
            @RequestParam("token") @Parameter(description = "Verification token", required = true) String token,
            @RequestParam("email") @Parameter(description = "User's email address", required = true) String email) {
        GenericResponseDTO<String> response = verificationService.verifyEmail(token, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Resend the email verification token")
    @ApiResponse(responseCode = "200", description = "Verification email sent", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Account already verified", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Error while sending the email", content = @Content(schema = @Schema(implementation = GenericResponseDTO.class)))
    @PostMapping("/resend-verification")
    public ResponseEntity<GenericResponseDTO<String>> resendVerification(@RequestParam("email") @Parameter(description = "User's email address", required = true) String email) {
        GenericResponseDTO<String> response = verificationService.resendVerification(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}