package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.RouteDTO;
import com.matiasugluck.deremate_backend.dto.route.CreateRouteDTO;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import com.matiasugluck.deremate_backend.service.RouteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


import java.util.List;

@RestController
@RequestMapping(value = "${base-path-v1}/routes", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<RouteDTO>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @PostMapping
    public ResponseEntity<RouteDTO> createRoute(@RequestBody CreateRouteDTO createRouteDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(createRouteDTO));
    }

    @PutMapping("/{routeId}/assign")
    public ResponseEntity<RouteDTO> assignRouteToUser(@PathVariable Long routeId, @RequestParam Long userId) {
        return ResponseEntity.ok(routeService.assignRouteToUser(routeId, userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RouteDTO>> getRoutesByUser(@PathVariable Long userId,
                                          @RequestParam(required = false) RouteStatus status) {
        List<RouteDTO> routes = (status != null)
                ? routeService.getRoutesByUserAndStatus(userId, status)
                : routeService.getRoutesByUser(userId);
        return ResponseEntity.ok(routes);
    }

    @PutMapping("/{routeId}/complete")
    public ResponseEntity<RouteDTO> completeRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(routeService.completeRoute(routeId));
    }

    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<List<RouteDTO>> getCompletedRoutesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(routeService.getRoutesByUserAndStatus(userId, RouteStatus.COMPLETED));
    }



}
