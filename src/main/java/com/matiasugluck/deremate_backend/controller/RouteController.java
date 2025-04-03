package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.AssignRouteRequest;
import com.matiasugluck.deremate_backend.dto.RouteDTO;
import com.matiasugluck.deremate_backend.entity.Route;
import com.matiasugluck.deremate_backend.service.RouteService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<RouteDTO> getAllRoutes() {
        return routeService.getAllRoutes().stream().map(RouteDTO::new).collect(Collectors.toList());
    }

    @PostMapping
    public RouteDTO createRoute(@RequestBody Route route) {
        return new RouteDTO(routeService.createRoute(route));
    }

    @PutMapping("/{routeId}/assign")
    public RouteDTO assignRouteToUser(@PathVariable Long routeId, @RequestParam Long userId) {
        return new RouteDTO(routeService.assignRouteToUser(routeId, userId));
    }

    @PostMapping("/assign")
    public RouteDTO assignRouteByQr(@RequestBody AssignRouteRequest request) {
        return new RouteDTO(routeService.assignRouteByQrCode(request.getRouteCode(), request.getUserId()));
    }

    @GetMapping("/user/{userId}")
    public List<RouteDTO> getRoutesByUser(@PathVariable Long userId,
                                          @RequestParam(required = false) String status) {
        List<Route> routes = (status != null && !status.isEmpty())
                ? routeService.getRoutesByUserAndStatus(userId, status)
                : routeService.getRoutesByUser(userId);
        return routes.stream().map(RouteDTO::new).collect(Collectors.toList());
    }

    @PutMapping("/{routeId}/complete")
    public RouteDTO completeRoute(@PathVariable Long routeId) {
        return new RouteDTO(routeService.completeRoute(routeId));
    }

    @GetMapping("/user/{userId}/completed")
    public List<RouteDTO> getCompletedRoutesByUser(@PathVariable Long userId) {
        return routeService.getRoutesByUserAndStatus(userId, "completada")
                .stream().map(RouteDTO::new).collect(Collectors.toList());
    }



}
