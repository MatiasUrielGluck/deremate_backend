package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.LoginRequestDTO;
import com.matiasugluck.deremate_backend.dto.auth.LoginResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.SignupRequestDTO;
import com.matiasugluck.deremate_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "${base-path-v1}/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

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
}
