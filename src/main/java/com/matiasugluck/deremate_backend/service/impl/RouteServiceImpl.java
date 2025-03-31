package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.entity.Route;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.repository.RouteRepository;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.service.RouteService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;

    public RouteServiceImpl(RouteRepository routeRepository, UserRepository userRepository) {
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Override
    public Route createRoute(Route route) {
        if (route.getQrCode() == null || route.getQrCode().isEmpty()) {
            route.setQrCode("RUTA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return routeRepository.save(route);
    }

    @Override
    public Route assignRouteToUser(Long routeId, Long userId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        route.setAssignedTo(user);
        route.setStatus("en_curso");

        return routeRepository.save(route);
    }

    @Override
    public Route assignRouteByQrCode(String qrCode, Long userId) {
        Route route = routeRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada con código QR"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        route.setAssignedTo(user);
        route.setStatus("en_curso");

        return routeRepository.save(route);
    }
}