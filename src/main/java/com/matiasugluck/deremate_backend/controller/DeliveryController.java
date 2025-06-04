package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.delivery.CreateDeliveryDTO;
import com.matiasugluck.deremate_backend.dto.delivery.DeliveryDTO;
import com.matiasugluck.deremate_backend.dto.delivery.PackageInWarehouseDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.exception.ApiError;
import com.matiasugluck.deremate_backend.service.AuthService;
import com.matiasugluck.deremate_backend.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "${base-path-v1}/delivery", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Delivery", description = "Endpoints para gestionar entregas")
public class DeliveryController {
    private final DeliveryService deliveryService;
    private final AuthService authService;

    @Operation(summary = "Crear una nueva entrega", description = "Registra una nueva solicitud de entrega con los productos asociados.")
    @ApiResponse(responseCode = "201", description = "Entrega creada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeliveryDTO.class)))
    @ApiResponse(responseCode = "400", description = "Solicitud inválida (p. ej., IDs de producto no válidos)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "500", description = "Error interno del servidor al crear la entrega", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public ResponseEntity<DeliveryDTO> createDelivery(@RequestBody CreateDeliveryDTO createDeliveryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryService.createDelivery(createDeliveryDTO));
    }

    @Operation(summary = "Confirmar una entrega", description = "Marca una entrega como completada usando su ID y el PIN de confirmación.")
    @ApiResponse(responseCode = "204", description = "Entrega confirmada exitosamente")
    @ApiResponse(responseCode = "400", description = "PIN inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Entrega no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmDelivery(@PathVariable Long id, @RequestParam(required = true) String pin) {
        deliveryService.confirmDelivery(id, pin);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cancelar una entrega", description = "Marca una entrega como rechazada/cancelada usando su ID.")
    @ApiResponse(responseCode = "204", description = "Entrega cancelada exitosamente")
    @ApiResponse(responseCode = "404", description = "Entrega no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelDelivery(@PathVariable Long id) {
        deliveryService.cancelDelivery(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener detalles de una entrega por ID", description = "Recupera la información completa de una entrega específica.")
    @ApiResponse(responseCode = "200", description = "Detalles de la entrega", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeliveryDTO.class)))
    @ApiResponse(responseCode = "404", description = "Entrega no encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryDTO> getDeliveryById(@PathVariable Long id) {
        return ResponseEntity.ok().body(deliveryService.getDeliveryById(id));
    }
    @Operation(summary = "Obtener paquetes en almacén", description = "Devuelve una lista de todos los paquetes que aún no han sido entregados y se encuentran en el almacén.")
    @ApiResponse(responseCode = "200", description = "Lista de paquetes en almacén", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PackageInWarehouseDTO.class)))
    @GetMapping("/warehouse")
    public ResponseEntity<List<PackageInWarehouseDTO>> getPackagesInWarehouse() {
        return ResponseEntity.ok(deliveryService.getPackagesInWarehouse());
    }

    @Operation(summary = "Obtener entregas por ID de usuario asignado", description = "Devuelve una lista de entregas asignadas a un usuario específico (p. ej., repartidor).")
    @ApiResponse(responseCode = "200", description = "Lista de entregas para el usuario especificado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeliveryDTO.class)))
    @GetMapping("/")
    public ResponseEntity<List<DeliveryDTO>> getPackagesByUserId() {
        User user = authService.getAuthenticatedUser();
        return ResponseEntity.ok(deliveryService.getDeliveriesByUserId(user.getId()));
    }

}
