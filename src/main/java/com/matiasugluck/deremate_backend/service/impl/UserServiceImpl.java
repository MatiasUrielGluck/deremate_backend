package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.dto.user.UserDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.service.AuthService;
import com.matiasugluck.deremate_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final AuthService authService;

    @Override
    public UserDTO getUserInfo() {
        User user = authService.getAuthenticatedUser();
        return user.toDTO();
    }
}
