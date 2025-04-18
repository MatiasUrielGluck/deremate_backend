package com.matiasugluck.deremate_backend;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.matiasugluck.deremate_backend.config.SecurityConfiguration;
import com.matiasugluck.deremate_backend.constants.AuthApiMessages;
import com.matiasugluck.deremate_backend.constants.ValidationApiMessages;
import com.matiasugluck.deremate_backend.controller.AuthController;
import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.SignupRequestDTO;
import com.matiasugluck.deremate_backend.service.AuthService;
import com.matiasugluck.deremate_backend.service.JwtService;
import com.matiasugluck.deremate_backend.service.impl.PasswordResetService;
import com.matiasugluck.deremate_backend.service.impl.VerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
public class SignUpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;


    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private VerificationService verificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @Test
    public void testRegisterSuccess() throws Exception {
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "Sebazafra@mail.com",
                "Holamundo12",
                "Seba",
                "Zafra"
        );
        GenericResponseDTO<String> successResponse = new GenericResponseDTO<>(
                AuthApiMessages.USER_REGISTERED_SUCCESSFULLY + "Sebazafra@mail.com",
                HttpStatus.CREATED.value()
        );

        when(authService.signup(
                signupRequestDTO.getEmail(),
                signupRequestDTO.getPassword(),
                signupRequestDTO.getFirstName(),
                signupRequestDTO.getLastName()
        )).thenReturn(successResponse);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDTO)).with(csrf()).with(anonymous()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(AuthApiMessages.USER_REGISTERED_SUCCESSFULLY + "Sebazafra@mail.com"));
    }

    @Test
    public void testRegisterFailureEmptyPassword() throws Exception {

        SignupRequestDTO invalidDto = new SignupRequestDTO(
                "test@test.com",
                "",
                "Test",
                "User"
        );


        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .with(csrf()).with(anonymous()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.password").exists())
                .andExpect(jsonPath("$.data.password", containsString(ValidationApiMessages.FIELD_CANNOT_BE_BLANK)))
                .andExpect(jsonPath("$.data.password", containsString(ValidationApiMessages.PASSWORD_TOO_SHORT.replace("{min}","6"))))
                .andExpect(jsonPath("$.data.password", containsString(ValidationApiMessages.PASSWORD_REQUIREMENTS)));


    }



}
