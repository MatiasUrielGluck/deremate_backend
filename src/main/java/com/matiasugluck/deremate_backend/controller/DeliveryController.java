package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.delivery.CreateDeliveryDTO;
import com.matiasugluck.deremate_backend.dto.delivery.DeliveryDTO;
import com.matiasugluck.deremate_backend.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "${base-path-v1}/delivery", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryDTO> createDelivery(@RequestBody CreateDeliveryDTO createDeliveryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryService.createDelivery(createDeliveryDTO));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmDelivery(@PathVariable Long id, @RequestParam(required = true) String pin) {
        deliveryService.confirmDelivery(id, pin);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelDelivery(@PathVariable Long id) {
        deliveryService.cancelDelivery(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryDTO> getDeliveryById(@PathVariable Long id) {
        return ResponseEntity.ok().body(deliveryService.getDeliveryById(id));
    }
}
