package com.matiasugluck.deremate_backend.service;

import com.matiasugluck.deremate_backend.dto.RouteDTO;
import com.matiasugluck.deremate_backend.dto.route.AvailableRouteDTO;
import com.matiasugluck.deremate_backend.dto.route.CreateRouteDTO;
import com.matiasugluck.deremate_backend.enums.RouteStatus;

import java.util.List;

public interface RouteService {
    List<RouteDTO> getAllRoutes();
    RouteDTO createRoute(CreateRouteDTO createRouteDTO);
    RouteDTO assignRouteToUser(Long routeId, Long userId);
    List<RouteDTO> getRoutesByUser(Long userId);
    List<RouteDTO> getRoutesByUserAndStatus(Long userId, RouteStatus status);
    RouteDTO completeRoute(Long routeId);
    List<AvailableRouteDTO> getAvailableRoutes(String originNeighborhood, String destinationNeighborhood);

}
