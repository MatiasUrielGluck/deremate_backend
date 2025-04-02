package com.matiasugluck.deremate_backend.repository;

import com.matiasugluck.deremate_backend.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByQrCode(String qrCode);

}
