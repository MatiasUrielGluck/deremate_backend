package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.RouteDTO;
import com.matiasugluck.deremate_backend.dto.route.AvailableRouteDTO;
import com.matiasugluck.deremate_backend.dto.route.CreateRouteDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import com.matiasugluck.deremate_backend.exception.ApiError;
import com.matiasugluck.deremate_backend.service.AuthService;
import com.matiasugluck.deremate_backend.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


import java.util.List;

@RestController
@RequestMapping(value = "${base-path-v1}/routes", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
@Tag(name = "Routes", description = "Endpoints para gestionar rutas de entrega")
public class RouteController {

    private final RouteService routeService;
    private final AuthService authService;

    public RouteController(RouteService routeService, AuthService authService) {
        this.routeService = routeService;
        this.authService = authService;
    }

    @Operation(summary = "Obtener todas las rutas", description = "Devuelve una lista de todas las rutas registradas en el sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de rutas obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteDTO.class)))
    @GetMapping
    public ResponseEntity<List<RouteDTO>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @Operation(summary = "Crear una nueva ruta", description = "Registra una nueva ruta pendiente en el sistema.")
    @ApiResponse(responseCode = "201", description = "Ruta creada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteDTO.class)))
    @PostMapping
    public ResponseEntity<RouteDTO> createRoute(@RequestBody CreateRouteDTO createRouteDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(createRouteDTO));
    }

    @Operation(summary = "Asignar una ruta a un usuario", description = "Asigna una ruta pendiente a un usuario específico y la marca como iniciada.")
    @ApiResponse(responseCode = "200", description = "Ruta asignada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteDTO.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud inválida (p. ej., usuario no encontrado, ruta no pendiente, usuario ya tiene ruta activa)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Ruta no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{routeId}/assign")
    public ResponseEntity<RouteDTO> assignRouteToUser(@PathVariable Long routeId, @RequestParam Long userId) {
        return ResponseEntity.ok(routeService.assignRouteToUser(routeId, userId));
    }

    @Operation(summary = "Obtener rutas por usuario", description = "Devuelve las rutas asignadas a un usuario, opcionalmente filtradas por estado.")
    @ApiResponse(responseCode = "200", description = "Lista de rutas del usuario obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteDTO.class)))
    @GetMapping("/")
    public ResponseEntity<List<RouteDTO>> getRoutesByUser(@RequestParam(required = false) RouteStatus status) {
        User user = authService.getAuthenticatedUser();
        List<RouteDTO> routes = (status != null)
                ? routeService.getRoutesByUserAndStatus(user.getId(), status)
                : routeService.getRoutesByUser(user.getId());
        return ResponseEntity.ok(routes);
    }


    @Operation(summary = "Completar una ruta", description = "Marca una ruta iniciada como completada.")
    @ApiResponse(responseCode = "200", description = "Ruta completada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteDTO.class)))
    @ApiResponse(responseCode = "400", description = "No es posible completar la ruta (p. ej., no está iniciada)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Ruta no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{routeId}/complete")
    public ResponseEntity<RouteDTO> completeRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(routeService.completeRoute(routeId));
    }

    @Operation(summary = "Obtener rutas completadas por usuario", description = "Devuelve una lista de todas las rutas completadas asignadas a un usuario específico.")
    @ApiResponse(responseCode = "200", description = "Lista de rutas completadas obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RouteDTO.class)))
    @GetMapping("/completed")
    public ResponseEntity<List<RouteDTO>> getCompletedRoutesByUser() {
        User user = authService.getAuthenticatedUser();
        return ResponseEntity.ok(routeService.getRoutesByUserAndStatus(user.getId(), RouteStatus.COMPLETED));
    }

    @Operation(summary = "Obtener rutas disponibles para asignar", description = "Devuelve una lista de rutas pendientes que no han sido asignadas, opcionalmente filtradas por barrio de origen y/o destino.")
    @ApiResponse(responseCode = "200", description = "Lista de rutas disponibles obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AvailableRouteDTO.class))) // Springdoc infiere la lista
    @GetMapping("/available")
    public ResponseEntity<List<AvailableRouteDTO>> getAvailableRoutes(
            @RequestParam(required = false) String originBarrio,
            @RequestParam(required = false) String destinationBarrio) {
        List<AvailableRouteDTO> availableRoutes =
                routeService.getAvailableRoutes(originBarrio, destinationBarrio);
        return ResponseEntity.ok(availableRoutes);
    }






}
