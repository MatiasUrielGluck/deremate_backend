package com.matiasugluck.deremate_backend.repository;

import com.matiasugluck.deremate_backend.entity.Route;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    // Todas las rutas asignadas a un usuario
    List<Route> findByAssignedToId(Long userId);

    // Rutas asignadas a un usuario con estado específico
    List<Route> findByAssignedToIdAndStatus(Long userId, RouteStatus status);

    // Busca rutas disponibles para un repartidor
    List<Route> findByAssignedToIsNullAndStatus(RouteStatus status);
}
