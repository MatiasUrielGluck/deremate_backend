package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.constants.PointsApiMessages;
import com.matiasugluck.deremate_backend.dto.points.RewardSpinDTO;
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
        return ResponseEntity.ok(PointsApiMessages.POINTS_ADDED_DESC);
    }

    @PostMapping("/add/{userId}/distance")
    public ResponseEntity<String> addPointsByDistance(@PathVariable Long userId, @RequestParam double kilometers) {
        pointsService.addPointsByDistance(userId, kilometers);
        return ResponseEntity.ok(PointsApiMessages.POINTS_ADDED_BY_DISTANCE_DESC + ": " + kilometers + " km.");
    }

    @PostMapping("/{userId}/subtract")
    public ResponseEntity<String> subtractPoints(@PathVariable Long userId, @RequestParam int points) {
        pointsService.subtractPoints(userId, points);
        return ResponseEntity.ok(PointsApiMessages.POINTS_SUBTRACTED_DESC);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserPointsDTO> getUserPoints(@PathVariable Long userId) {
        return ResponseEntity.ok(pointsService.getUserPointsInfo(userId));
    }

    @GetMapping("/{userId}/progress")
    public ResponseEntity<Double> getProgressPercentage(@PathVariable Long userId) {
        return ResponseEntity.ok(pointsService.getProgressPercentage(userId));
    }

    @PostMapping("/{userId}/reset")
    public ResponseEntity<String> resetPoints(@PathVariable Long userId) {
        pointsService.resetPoints(userId);
        return ResponseEntity.ok(PointsApiMessages.POINTS_RESET_DESC);
    }

    @PostMapping("/{userId}/boost")
    public ResponseEntity<String> boostPoints(@PathVariable Long userId, @RequestParam int multiplier) {
        pointsService.boostPoints(userId, multiplier);
        return ResponseEntity.ok(PointsApiMessages.POINTS_BOOSTED_DESC + " x" + multiplier);
    }

    @PostMapping("/{userId}/spin")
    public ResponseEntity<RewardSpinDTO> spinWheel(@PathVariable Long userId) {
        return ResponseEntity.ok(pointsService.spinRewardWheel(userId));
    }


}








































































































































































































































