package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.dto.RouteDTO;
import com.matiasugluck.deremate_backend.dto.route.CreateRouteDTO;
import com.matiasugluck.deremate_backend.entity.Route;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import com.matiasugluck.deremate_backend.exception.ApiException;
import com.matiasugluck.deremate_backend.repository.RouteRepository;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final UserRepository userRepository;

    @Override
    public List<RouteDTO> getAllRoutes() {
        return routeRepository.findAll().stream().map(Route::toDto).toList();
    }

    @Override
    public RouteDTO createRoute(CreateRouteDTO createRouteDTO) {
        Route route = Route.builder()
                .origin("coordenadas de origen")
                .destination(createRouteDTO.getDestination())
                .status(RouteStatus.PENDING)
                .build();
        return routeRepository.save(route).toDto();
    }

    @Override
    public RouteDTO assignRouteToUser(Long routeId, Long userId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ApiException("ROUTE_NOT_FOUND", "Ruta no encontrada", HttpStatus.NOT_FOUND.value()));

        if (!route.getStatus().equals(RouteStatus.PENDING)) {
            throw new ApiException("INVALID_ROUTE_REQUEST", "Solo se pueden asignar rutas con estado 'pendiente'", HttpStatus.BAD_REQUEST.value());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "Usuario no encontrado", HttpStatus.BAD_REQUEST.value()));

        boolean hasActiveRoute = !routeRepository.findByAssignedToIdAndStatus(userId, RouteStatus.INITIATED).isEmpty();
        if (hasActiveRoute) {
            throw new ApiException("INVALID_ROUTE_REQUEST", "User already has active routes", HttpStatus.BAD_REQUEST.value());
        }

        route.setAssignedTo(user);
        route.setStatus(RouteStatus.INITIATED);

        return routeRepository.save(route).toDto();
    }

    @Override
    public List<RouteDTO> getRoutesByUser(Long userId) {
        return routeRepository.findByAssignedToId(userId).stream().map(Route::toDto).toList();
    }

    @Override
    public List<RouteDTO> getRoutesByUserAndStatus(Long userId, RouteStatus status) {
        return routeRepository.findByAssignedToIdAndStatus(userId, status).stream().map(Route::toDto).toList();
    }

    @Override
    public RouteDTO completeRoute(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));

        if (!route.getStatus().equals(RouteStatus.INITIATED)) {
            throw new IllegalStateException("Solo se pueden completar rutas en estado 'en_curso'");
        }

        route.setStatus(RouteStatus.COMPLETED);
        route.setCompletedAt(Timestamp.from(Instant.now()));

        return routeRepository.save(route).toDto();
    }

}
