package com.matiasugluck.deremate_backend.dto.auth;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class LoginResponseDTO extends GenericResponseDTO {
    private String token;
    private Long expiresIn;
}
