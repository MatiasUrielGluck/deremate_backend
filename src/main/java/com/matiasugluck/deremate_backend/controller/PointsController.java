package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.points.UserPointsDTO;
import com.matiasugluck.deremate_backend.service.PointsService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("${base-path-v1}/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @PostMapping("/add/{userId}")
    public ResponseEntity<String> addPoints(@PathVariable Long userId) {
        pointsService.addPointsForCompletedDelivery(userId);
        return ResponseEntity.ok("Puntos añadidos correctamente.");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserPointsDTO> getUserPoints(@PathVariable Long userId) {
        return ResponseEntity.ok(pointsService.getUserPointsInfo(userId));
    }















































































}