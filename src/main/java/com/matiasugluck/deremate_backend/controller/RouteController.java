package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.entity.Route;
import com.matiasugluck.deremate_backend.service.RouteService;
import org.springframework.web.bind.annotation.*;
import com.matiasugluck.deremate_backend.dto.AssignRouteRequest;


import java.util.List;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<Route> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    @PostMapping
    public Route createRoute(@RequestBody Route route) {
        return routeService.createRoute(route);
    }
    @PutMapping("/{routeId}/assign")
    public Route assignRouteToUser(@PathVariable Long routeId, @RequestParam Long userId) {
        return routeService.assignRouteToUser(routeId, userId);
    }

    @PostMapping("/assign")
    public Route assignRouteByQr(@RequestBody AssignRouteRequest request) {
        return routeService.assignRouteByQrCode(request.getRouteCode(), request.getUserId());
    }

}
