package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.LoginRequestDTO;
import com.matiasugluck.deremate_backend.dto.auth.LoginResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.PasswordResetRequestDto;
import com.matiasugluck.deremate_backend.dto.auth.SignupRequestDTO;
import com.matiasugluck.deremate_backend.service.AuthService;
import com.matiasugluck.deremate_backend.service.impl.PasswordResetService;
import com.matiasugluck.deremate_backend.service.impl.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "${base-path-v1}/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final VerificationService verificationService;

    @PostMapping("/login")
    public ResponseEntity<GenericResponseDTO<Object>> login(@RequestBody @Validated LoginRequestDTO loginRequestDTO) {
        GenericResponseDTO<Object> response = authService.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<GenericResponseDTO<String>> signup(@RequestBody @Validated SignupRequestDTO signupRequestDTO) {
        GenericResponseDTO<String> response = authService.signup(signupRequestDTO.getEmail(), signupRequestDTO.getPassword());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GenericResponseDTO<String>> forgotPassword(@RequestParam("email") String email) {
        GenericResponseDTO<String> response = passwordResetService.sendPasswordResetToken(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GenericResponseDTO<String>> resetPassword(@RequestBody PasswordResetRequestDto request) {
        GenericResponseDTO<String> response = passwordResetService.resetPassword(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<GenericResponseDTO<String>> verifyEmail(@RequestParam("token") String token, @RequestParam("email") String email) {
        GenericResponseDTO<String> response = verificationService.verifyEmail(token, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<GenericResponseDTO<String>> resendVerification(@RequestParam("email") String email) {
        GenericResponseDTO<String> response = verificationService.resendVerification(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}