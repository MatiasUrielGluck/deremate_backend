package com.matiasugluck.deremate_backend.controller;

import com.matiasugluck.deremate_backend.dto.points.PointsDTO;
import com.matiasugluck.deremate_backend.dto.points.PointsTransferDTO;
import com.matiasugluck.deremate_backend.dto.points.PointsOperationDTO;
import com.matiasugluck.deremate_backend.dto.points.PointsStatisticsDTO;
import com.matiasugluck.deremate_backend.service.PointsService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.*;

@RestController
@RequestMapping("${base-path-v1}/points")
@RequiredArgsConstructor
public class PointsController {
    private final PointsService pointsService;

    /**
     * Adds points to a user account
     * @param pointsDTO The points data to add
     * @return Response with status message
     */
    @PostMapping("/add")
    public ResponseEntity<String> addPoints(@RequestBody PointsDTO pointsDTO) {
        pointsService.addPoints(pointsDTO);
        return ResponseEntity.ok("Points added successfully");
    }

    /**
     * Get points for a specific user
     * @param userId The user ID to query
     * @return The points data for the user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<PointsDTO> getPoints(@PathVariable Long userId) {
        return ResponseEntity.ok(pointsService.getPoints(userId));
    }

    /**
     * Reset points for a specific user
     * @param userId The user ID to reset points for
     * @return Response with status message
     */
    @DeleteMapping("/{userId}/reset")
    public ResponseEntity<String> resetPoints(@PathVariable Long userId) {
        pointsService.resetPoints(userId);
        return ResponseEntity.ok("Points reset successfully");
    }

    /**
     * Multiply points for a specific user
     * @param userId The user ID
     * @param operation The operation details containing the multiplication factor
     * @return Response with status message
     */
    @PutMapping("/{userId}/multiply")
    public ResponseEntity<String> multiplyPoints(
            @PathVariable Long userId,
            @RequestBody PointsOperationDTO operation) {
        pointsService.multiplyPoints(userId, operation.getFactor());
        return ResponseEntity.ok("Points multiplied successfully");
    }

    /**
     * Divide points for a specific user
     * @param userId The user ID
     * @param operation The operation details containing the divisor
     * @return Response with status message
     */
    @PutMapping("/{userId}/divide")
    public ResponseEntity<String> dividePoints(
            @PathVariable Long userId,
            @RequestBody PointsOperationDTO operation) {
        if (operation.getFactor() == 0) {
            return ResponseEntity.badRequest().body("Divisor cannot be zero");
        }
        pointsService.dividePoints(userId, operation.getFactor());
        return ResponseEntity.ok("Points divided successfully");
    }

    /**
     * Check if a user has points
     * @param userId The user ID to check
     * @return Boolean indicating if the user has points
     */
    @GetMapping("/{userId}/has-points")
    public ResponseEntity<Boolean> hasPoints(@PathVariable Long userId) {
        return ResponseEntity.ok(pointsService.hasPoints(userId));
    }

    /**
     * Transfer points between users
     * @param transfer The transfer details
     * @return Response with status message
     */
    @PostMapping("/transfer")
    public ResponseEntity<String> transferPoints(@RequestBody PointsTransferDTO transfer) {
        pointsService.transferPoints(transfer.getFromUserId(), transfer.getToUserId(), transfer.getAmount());
        return ResponseEntity.ok("Points transferred successfully");
    }

    /**
     * Get total points in the system
     * @return The total points count
     */
    @GetMapping("/total")
    public ResponseEntity<Integer> getTotalPoints() {
        return ResponseEntity.ok(pointsService.getTotalPoints());
    }

    /**
     * Get top users by points
     * @param limit The number of top users to return
     * @return List of top users with their points
     */
    @GetMapping("/top")
    public ResponseEntity<List<PointsDTO>> getTopUsers(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(pointsService.getTopUsers(limit));
    }

    /**
     * Get all users with points with pagination
     * @param pageable Pagination parameters
     * @return Page of users with points
     */
    @GetMapping("/all")
    public ResponseEntity<Page<PointsDTO>> getAllUsersWithPoints(
            @PageableDefault(size = 20, sort = "points", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pointsService.getAllUsersWithPoints(pageable));
    }

    /**
     * Award bonus points to user
     * @param userId The user ID
     * @param bonus The bonus points to add
     * @return Response with status message
     */
    @PostMapping("/{userId}/bonus")
    public ResponseEntity<String> awardBonusPoints(
            @PathVariable Long userId,
            @RequestParam int bonus) {
        pointsService.awardBonusPoints(userId, bonus);
        return ResponseEntity.ok("Bonus points awarded successfully");
    }

    /**
     * Apply penalty to user's points
     * @param userId The user ID
     * @param penalty The penalty points to deduct
     * @return Response with status message
     */
    @PostMapping("/{userId}/penalty")
    public ResponseEntity<String> applyPointsPenalty(
            @PathVariable Long userId,
            @RequestParam int penalty) {
        pointsService.applyPointsPenalty(userId, penalty);
        return ResponseEntity.ok("Points penalty applied successfully");
    }

    /**
     * Set minimum points threshold for all users
     * @param threshold The minimum points threshold
     * @return Response with status message
     */
    @PostMapping("/threshold/minimum")
    public ResponseEntity<String> setMinimumPointsThreshold(@RequestParam int threshold) {
        pointsService.setMinimumPointsThreshold(threshold);
        return ResponseEntity.ok("Minimum points threshold set successfully");
    }

    /**
     * Set maximum points cap for all users
     * @param cap The maximum points cap
     * @return Response with status message
     */
    @PostMapping("/threshold/maximum")
    public ResponseEntity<String> setMaximumPointsCap(@RequestParam int cap) {
        pointsService.setMaximumPointsCap(cap);
        return ResponseEntity.ok("Maximum points cap set successfully");
    }

    /**
     * Get users with points in specified range
     * @param min Minimum points value
     * @param max Maximum points value
     * @return List of users with points in the range
     */
    @GetMapping("/range")
    public ResponseEntity<List<PointsDTO>> getUsersInPointsRange(
            @RequestParam int min,
            @RequestParam int max) {
        return ResponseEntity.ok(pointsService.getUsersInPointsRange(min, max));
    }

    /**
     * Reset all users' points
     * @return Response with status message
     */
    @DeleteMapping("/reset-all")
    public ResponseEntity<String> resetAllPoints() {
        pointsService.resetAllPoints();
        return ResponseEntity.ok("All points reset successfully");
    }

    /**
     * Apply percentage increase to all users' points
     * @param percentage The percentage to increase points by
     * @return Response with status message
     */
    @PutMapping("/increase-all")
    public ResponseEntity<String> increaseAllPointsByPercentage(@RequestParam double percentage) {
        pointsService.increaseAllPointsByPercentage(percentage);
        return ResponseEntity.ok("All points increased by percentage successfully");
    }

    /**
     * Apply percentage decrease to all users' points
     * @param percentage The percentage to decrease points by
     * @return Response with status message
     */
    @PutMapping("/decrease-all")
    public ResponseEntity<String> decreaseAllPointsByPercentage(@RequestParam double percentage) {
        pointsService.decreaseAllPointsByPercentage(percentage);
        return ResponseEntity.ok("All points decreased by percentage successfully");
    }

    /**
     * Get points statistics
     * @return Statistics about points distribution
     */
    @GetMapping("/statistics")
    public ResponseEntity<PointsStatisticsDTO> getPointsStatistics() {
        return ResponseEntity.ok(pointsService.getPointsStatistics());
    }

    /**
     * Convert user points to rewards
     * @param userId The user ID
     * @param pointsToConvert Points to convert to rewards
     * @return Response with status message
     */
    @PostMapping("/{userId}/convert-to-rewards")
    public ResponseEntity<String> convertPointsToRewards(
            @PathVariable Long userId,
            @RequestParam int pointsToConvert) {
        pointsService.convertPointsToRewards(userId, pointsToConvert);
        return ResponseEntity.ok("Points converted to rewards successfully");
    }

    /**
     * Get user's points history
     * @param userId The user ID
     * @return List of points history entries
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<List<PointsDTO>> getUserPointsHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(pointsService.getUserPointsHistory(userId));
    }

    /**
     * Expire points older than specified days
     * @param days Number of days before points expire
     * @return Response with status message
     */
    @PostMapping("/expire")
    public ResponseEntity<String> expirePoints(@RequestParam int days) {
        pointsService.expirePoints(days);
        return ResponseEntity.ok("Expired points successfully");
    }

    /**
     * Calculate tier level based on points
     * @param userId The user ID
     * @return The user's loyalty tier level
     */
    @GetMapping("/{userId}/tier")
    public ResponseEntity<Integer> calculateUserTier(@PathVariable Long userId) {
        return ResponseEntity.ok(pointsService.calculateUserTier(userId));
    }

    /**
     * Export all users points data
     * @return Map of all users and their points
     */
    @GetMapping("/export")
    public ResponseEntity<Map<Long, PointsDTO>> exportAllPointsData() {
        return ResponseEntity.ok(pointsService.exportAllPointsData());
    }

    /**
     * Import points data
     * @param pointsData Map of user IDs to points data
     * @return Response with status message
     */
    @PostMapping("/import")
    public ResponseEntity<String> importPointsData(@RequestBody Map<Long, PointsDTO> pointsData) {
        pointsService.importPointsData(pointsData);
        return ResponseEntity.ok("Points data imported successfully");
    }

    /**
     * Apply seasonal bonus to all users
     * @param seasonName The season name
     * @param multiplier The bonus multiplier
     * @return Response with status message
     */
    @PostMapping("/seasonal-bonus")
    public ResponseEntity<String> applySeasonalBonus(
            @RequestParam String seasonName,
            @RequestParam double multiplier) {
        pointsService.applySeasonalBonus(seasonName, multiplier);
        return ResponseEntity.ok("Seasonal bonus applied successfully");
    }

    /**
     * Get leaderboard for specific time period
     * @param startDate Start date in ISO format
     * @param endDate End date in ISO format
     * @param limit Number of users to include
     * @return List of top users for the period
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<PointsDTO>> getLeaderboardForPeriod(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(pointsService.getLeaderboardForPeriod(startDate, endDate, limit));
    }

    /**
     * Calculate points needed to reach next tier
     * @param userId The user ID
     * @return Points needed for next tier
     */
    @GetMapping("/{userId}/next-tier")
    public ResponseEntity<Integer> calculatePointsForNextTier(@PathVariable Long userId) {
        return ResponseEntity.ok(pointsService.calculatePointsForNextTier(userId));
    }

    /**
     * Merge points from multiple users into one
     * @param targetUserId The target user ID
     * @param sourceUserIds List of source user IDs
     * @return Response with status message
     */
    @PostMapping("/merge")
    public ResponseEntity<String> mergeUserPoints(
            @RequestParam Long targetUserId,
            @RequestParam List<Long> sourceUserIds) {
        pointsService.mergeUserPoints(targetUserId, sourceUserIds);
        return ResponseEntity.ok("User points merged successfully");
    }

    /**
     * Split user points to multiple users
     * @param sourceUserId The source user ID
     * @param targetUserIds List of target user IDs
     * @param distribution List of distribution percentages
     * @return Response with status message
     */
    @PostMapping("/split")
    public ResponseEntity<String> splitUserPoints(
            @RequestParam Long sourceUserId,
            @RequestParam List<Long> targetUserIds,
            @RequestParam List<Double> distribution) {
        pointsService.splitUserPoints(sourceUserId, targetUserIds, distribution);
        return ResponseEntity.ok("User points split successfully");
    }

    /**
     * Get users below minimum threshold
     * @param threshold The minimum threshold
     * @return List of users below threshold
     */
    @GetMapping("/below-threshold")
    public ResponseEntity<List<PointsDTO>> getUsersBelowThreshold(@RequestParam int threshold) {
        return ResponseEntity.ok(pointsService.getUsersBelowThreshold(threshold));
    }

    /**
     * Get users above maximum threshold
     * @param threshold The maximum threshold
     * @return List of users above threshold
     */
    @GetMapping("/above-threshold")
    public ResponseEntity<List<PointsDTO>> getUsersAboveThreshold(@RequestParam int threshold) {
        return ResponseEntity.ok(pointsService.getUsersAboveThreshold(threshold));
    }

    /**
     * Apply points adjustment based on user activity
     * @param userId The user ID
     * @param activityLevel Activity level from 1-10
     * @return Response with status message
     */
    @PostMapping("/{userId}/activity-adjustment")
    public ResponseEntity<String> adjustPointsByActivity(
            @PathVariable Long userId,
            @RequestParam int activityLevel) {
        pointsService.adjustPointsByActivity(userId, activityLevel);
        return ResponseEntity.ok("Points adjusted by activity successfully");
    }

    /**
     * Get average points per user
     * @return Average points value
     */
    @GetMapping("/average")
    public ResponseEntity<Double> getAveragePointsPerUser() {
        return ResponseEntity.ok(pointsService.getAveragePointsPerUser());
    }

    /**
     * Get median points value
     * @return Median points value
     */
    @GetMapping("/median")
    public ResponseEntity<Integer> getMedianPointsValue() {
        return ResponseEntity.ok(pointsService.getMedianPointsValue());
    }

















































































































}