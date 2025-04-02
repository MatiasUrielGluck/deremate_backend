package com.matiasugluck.deremate_backend.service;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.entity.User;

public interface AuthService {
    GenericResponseDTO<Object> login(String email, String password);

    GenericResponseDTO signup(String email, String password);

    User getAuthenticatedUser();
}
