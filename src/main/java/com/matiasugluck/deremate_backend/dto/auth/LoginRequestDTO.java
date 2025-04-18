package com.matiasugluck.deremate_backend.dto.auth;

import com.matiasugluck.deremate_backend.constants.ValidationApiMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequestDTO {

    @NotBlank(message = ValidationApiMessages.FIELD_CANNOT_BE_BLANK)
    @Email(message = ValidationApiMessages.INVALID_EMAIL_FORMAT)
    private String email;

    @NotBlank(message = ValidationApiMessages.FIELD_CANNOT_BE_BLANK)
    private String password;
}
