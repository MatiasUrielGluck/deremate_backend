package com.matiasugluck.deremate_backend.dto.auth;

import com.matiasugluck.deremate_backend.constants.ValidationApiMessages;
import jakarta.validation.constraints.*;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@Getter
@Setter
public class SignupRequestDTO {
    @Email @NotEmpty
    private String email;

    @NotEmpty(message = ValidationApiMessages.FIELD_CANNOT_BE_BLANK)
    @Size(min = 6, message=ValidationApiMessages.PASSWORD_TOO_SHORT)
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).{6,}$",
            message = ValidationApiMessages.PASSWORD_REQUIREMENTS
    )
    private String password;

    @NotBlank(message = ValidationApiMessages.FIELD_CANNOT_BE_BLANK)
    private String firstName;

    @NotBlank(message = ValidationApiMessages.FIELD_CANNOT_BE_BLANK)
    private String lastName;
}
