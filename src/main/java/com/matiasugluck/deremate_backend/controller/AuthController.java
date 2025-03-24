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
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Validated LoginRequestDTO loginRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));
    }

    @PostMapping("/signup")
    public ResponseEntity<GenericResponseDTO> signup(@RequestBody @Validated SignupRequestDTO signupRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.signup(signupRequestDTO.getEmail(), signupRequestDTO.getPassword()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GenericResponseDTO> forgotPassword(@RequestParam("email") String email) {
        GenericResponseDTO response = passwordResetService.sendPasswordResetToken(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GenericResponseDTO> resetPassword(@RequestBody PasswordResetRequestDto request) {
        GenericResponseDTO response = passwordResetService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public GenericResponseDTO verifyEmail(@RequestParam("token") String token, @RequestParam("email") String email) {
        return verificationService.verifyEmail(token, email);
    }

    @PostMapping("/resend-verification")
    public GenericResponseDTO resendVerification(@RequestParam("email") String email) {
        return verificationService.resendVerification(email);
    }
}
