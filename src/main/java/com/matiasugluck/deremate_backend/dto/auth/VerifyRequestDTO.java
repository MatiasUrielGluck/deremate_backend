package com.matiasugluck.deremate_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class VerifyRequestDTO {
	private String token;
	private String email;
}
