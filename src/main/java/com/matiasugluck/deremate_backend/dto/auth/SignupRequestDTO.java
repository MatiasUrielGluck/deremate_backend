package com.matiasugluck.deremate_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class SignupRequestDTO {
    @Email @NotNull @NotEmpty
    private String email;

    @NotNull
    @NotEmpty
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).{6,}$",
            message = "Password must contain at least one uppercase letter and one number"
    )
    private String password;

    @NotNull
    @NotEmpty
    private String firstName;

    @NotNull
    @NotEmpty
    private String lastName;
}
