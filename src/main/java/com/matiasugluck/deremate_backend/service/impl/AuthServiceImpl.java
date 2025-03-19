package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.dto.auth.LoginResponseDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.exception.ApiException;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.service.AuthService;
import com.matiasugluck.deremate_backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @Override
    public LoginResponseDTO login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );

        User customer = userRepository.findByEmail(email)
                .orElseThrow();

        String jwtToken = jwtService.generateToken(customer);
        return LoginResponseDTO.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }

    @Override
    public GenericResponseDTO signup(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new ApiException("resource_already_exists", "Email already exists.", HttpStatus.BAD_REQUEST.value());
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        userRepository.save(user);
        return new GenericResponseDTO("ok");
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
