package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.entity.Route;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.repository.RouteRepository;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.service.RouteService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        if (!"pendiente".equals(route.getStatus())) {
            throw new IllegalStateException("Solo se pueden asignar rutas con estado 'pendiente'");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean hasActiveRoute = routeRepository.findByAssignedToIdAndStatus(userId, "en_curso").size() > 0;
        if (hasActiveRoute) {
            throw new IllegalStateException("El usuario ya tiene una ruta en curso");
        }

        route.setAssignedTo(user);
        route.setStatus("en_curso");

        return routeRepository.save(route);
    }

    @Override
    public Route assignRouteByQrCode(String qrCode, Long userId) {
        Route route = routeRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada con código QR"));

        if (!"pendiente".equals(route.getStatus())) {
            throw new IllegalStateException("Solo se pueden asignar rutas con estado 'pendiente'");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean hasActiveRoute = routeRepository.findByAssignedToIdAndStatus(userId, "en_curso").size() > 0;
        if (hasActiveRoute) {
            throw new IllegalStateException("El usuario ya tiene una ruta en curso");
        }

        route.setAssignedTo(user);
        route.setStatus("en_curso");

        return routeRepository.save(route);
    }

    @Override
    public List<Route> getRoutesByUser(Long userId) {
        return routeRepository.findByAssignedToId(userId);
    }

    @Override
    public List<Route> getRoutesByUserAndStatus(Long userId, String status) {
        return routeRepository.findByAssignedToIdAndStatus(userId, status);
    }

    @Override
    public Route completeRoute(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));

        if (!"en_curso".equals(route.getStatus())) {
            throw new IllegalStateException("Solo se pueden completar rutas en estado 'en_curso'");
        }

        route.setStatus("completada");
        route.setCompletedAt(LocalDateTime.now());

        return routeRepository.save(route);
    }

}