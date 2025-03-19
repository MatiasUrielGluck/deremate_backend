package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.user.UserDTO;
import com.matiasugluck.deremate_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "${base-path-v1}/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<UserDTO> getUserInfo() {
        return ResponseEntity.ok(userService.getUserInfo());
    }
}
