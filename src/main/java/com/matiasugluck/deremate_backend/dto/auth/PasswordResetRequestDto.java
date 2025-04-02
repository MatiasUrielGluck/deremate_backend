package com.matiasugluck.deremate_backend.dto.auth;

import lombok.Data;

@Data
public class PasswordResetRequestDto {
    private String token;
    private String email;
    private String password;
}
