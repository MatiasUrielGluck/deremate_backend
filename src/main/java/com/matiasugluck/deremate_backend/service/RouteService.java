package com.matiasugluck.deremate_backend.service;

import com.matiasugluck.deremate_backend.entity.Route;

import java.util.List;

public interface RouteService {
    List<Route> getAllRoutes();
    Route createRoute(Route route);
    Route assignRouteToUser(Long routeId, Long userId);
    Route assignRouteByQrCode(String qrCode, Long userId);

}
