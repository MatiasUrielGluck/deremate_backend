package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.dto.RouteDTO;
import com.matiasugluck.deremate_backend.dto.user.UserDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import com.matiasugluck.deremate_backend.service.AuthService;
import com.matiasugluck.deremate_backend.service.RouteService;
import com.matiasugluck.deremate_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final AuthService authService;
    private final RouteService routeService;

    @Override
    public UserDTO getUserInfo() {
        User user = authService.getAuthenticatedUser();
        List<RouteDTO> completedRoutes = routeService.getRoutesByUserAndStatus(user.getId(), RouteStatus.COMPLETED);

        int deliveriesCompleted = (completedRoutes != null) ? completedRoutes.size() : 0;

        UserDTO userDTO = user.toDTO();
        userDTO.setDeliveriesCompleted(deliveriesCompleted);
        return userDTO;
    }
}
