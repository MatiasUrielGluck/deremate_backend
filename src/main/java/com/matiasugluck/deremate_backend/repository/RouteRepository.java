package com.matiasugluck.deremate_backend.repository;

import com.matiasugluck.deremate_backend.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findByQrCode(String qrCode);

    // 🔍 Todas las rutas asignadas a un usuario
    List<Route> findByAssignedToId(Long userId);

    // 🔍 Rutas asignadas a un usuario con estado específico
    List<Route> findByAssignedToIdAndStatus(Long userId, String status);
}
