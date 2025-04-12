package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.dto.points.PointsDTO;
import com.matiasugluck.deremate_backend.dto.points.PointsHistoryEntryDTO;
import com.matiasugluck.deremate_backend.dto.points.PointsStatisticsDTO;
import com.matiasugluck.deremate_backend.service.PointsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PointsServiceImpl implements PointsService {
    private static final Logger logger = LoggerFactory.getLogger(PointsServiceImpl.class);

    private final Map<Long, PointsDTO> pointsMap = new ConcurrentHashMap<>();
    private final Map<Long, List<PointsHistoryEntryDTO>> pointsHistoryMap = new ConcurrentHashMap<>();
    private final Map<String, List<PointsDTO>> periodLeaderboardMap = new ConcurrentHashMap<>();

    private int minimumPointsThreshold = 0;
    private int maximumPointsCap = Integer.MAX_VALUE;

    // Tier thresholds
    private final Map<Integer, String> tierMap = new HashMap<>();

    // Constructor to set up initial tier configuration
    public PointsServiceImpl() {
        // Initialize tier thresholds
        tierMap.put(0, "Bronze");
        tierMap.put(1000, "Silver");
        tierMap.put(5000, "Gold");
        tierMap.put(10000, "Platinum");
        tierMap.put(25000, "Diamond");
        tierMap.put(50000, "Master");
    }

    /**
     * Add points to a user account
     */
    @Override
    public void addPoints(PointsDTO pointsDTO) {
        if (pointsDTO == null || pointsDTO.getUserId() == null) {
            throw new IllegalArgumentException("Points data or user ID cannot be null");
        }

        PointsDTO existingPoints = pointsMap.get(pointsDTO.getUserId());

        if (existingPoints != null) {
            // Record history before update
            recordPointsHistory(existingPoints.getUserId(), existingPoints.getPoints(),
                    existingPoints.getPoints() + pointsDTO.getPoints(), "ADD");

            // Update existing points
            existingPoints.setPoints(Math.min(existingPoints.getPoints() + pointsDTO.getPoints(), maximumPointsCap));
            existingPoints.setLastUpdated(LocalDateTime.now());
            existingPoints.setTierName(calculateTierName(existingPoints.getPoints()));
        } else {
            // Create new points entry
            pointsDTO.setLastUpdated(LocalDateTime.now());
            pointsDTO.setTierName(calculateTierName(pointsDTO.getPoints()));
            pointsDTO.setPoints(Math.min(pointsDTO.getPoints(), maximumPointsCap));
            pointsMap.put(pointsDTO.getUserId(), pointsDTO);

            // Record initial points history
            recordPointsHistory(pointsDTO.getUserId(), 0, pointsDTO.getPoints(), "INITIAL");
        }

        logger.info("Added {} points to user {}", pointsDTO.getPoints(), pointsDTO.getUserId());
    }

    /**
     * Get points for a specific user
     */
    @Override
    public PointsDTO getPoints(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        logger.debug("Getting points for user {}", userId);
        return pointsMap.getOrDefault(userId, new PointsDTO(userId, 0, LocalDateTime.now(), "Bronze", true));
    }

    /**
     * Reset points for a specific user
     */
    @Override
    public void resetPoints(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        PointsDTO existingPoints = pointsMap.get(userId);

        if (existingPoints != null) {
            // Record history before reset
            recordPointsHistory(existingPoints.getUserId(), existingPoints.getPoints(), 0, "RESET");

            // Remove points from map
            pointsMap.remove(userId);
            logger.info("Reset points for user {}", userId);
        }
    }

    /**
     * Multiply points for a user by a factor
     */
    @Override
    public void multiplyPoints(Long userId, int factor) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        PointsDTO points = pointsMap.get(userId);
        if (points != null) {
            int oldPoints = points.getPoints();
            int newPoints = Math.min(oldPoints * factor, maximumPointsCap);

            // Record history before update
            recordPointsHistory(userId, oldPoints, newPoints, "MULTIPLY");

            // Update points
            points.setPoints(newPoints);
            points.setLastUpdated(LocalDateTime.now());
            points.setTierName(calculateTierName(newPoints));

            logger.info("Multiplied points for user {} by factor {}", userId, factor);
        }
    }

    /**
     * Divide points for a user by a divisor
     */
    @Override
    public void dividePoints(Long userId, int divisor) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }

        PointsDTO points = pointsMap.get(userId);
        if (points != null) {
            int oldPoints = points.getPoints();
            int newPoints = Math.max(oldPoints / divisor, minimumPointsThreshold);

            // Record history before update
            recordPointsHistory(userId, oldPoints, newPoints, "DIVIDE");

            // Update points
            points.setPoints(newPoints);
            points.setLastUpdated(LocalDateTime.now());
            points.setTierName(calculateTierName(newPoints));

            logger.info("Divided points for user {} by divisor {}", userId, divisor);
        }
    }

    /**
     * Check if a user has points
     */
    @Override
    public boolean hasPoints(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return pointsMap.containsKey(userId);
    }

    /**
     * Transfer points from one user to another
     */
    @Override
    public void transferPoints(Long fromUserId, Long toUserId, int amount) {
        if (fromUserId == null || toUserId == null) {
            throw new IllegalArgumentException("User IDs cannot be null");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        PointsDTO fromUser = pointsMap.get(fromUserId);
        PointsDTO toUser = pointsMap.get(toUserId);

        if (fromUser == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source user not found: " + fromUserId);
        }

        if (fromUser.getPoints() < amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient points for transfer");
        }

        // Create toUser if it doesn't exist
        if (toUser == null) {
            toUser = new PointsDTO(toUserId, 0, LocalDateTime.now(), "Bronze", true);
            pointsMap.put(toUserId, toUser);
        }

        // Record history for source user
        recordPointsHistory(fromUserId, fromUser.getPoints(), fromUser.getPoints() - amount, "TRANSFER_OUT");

        // Record history for target user
        recordPointsHistory(toUserId, toUser.getPoints(), toUser.getPoints() + amount, "TRANSFER_IN");

        // Update points
        fromUser.setPoints(fromUser.getPoints() - amount);
        fromUser.setLastUpdated(LocalDateTime.now());
        fromUser.setTierName(calculateTierName(fromUser.getPoints()));

        toUser.setPoints(Math.min(toUser.getPoints() + amount, maximumPointsCap));
        toUser.setLastUpdated(LocalDateTime.now());
        toUser.setTierName(calculateTierName(toUser.getPoints()));

        logger.info("Transferred {} points from user {} to user {}", amount, fromUserId, toUserId);
    }

    /**
     * Get total points in the system
     */
    @Override
    public int getTotalPoints() {
        return pointsMap.values().stream().mapToInt(PointsDTO::getPoints).sum();
    }

    /**
     * Get top users by points
     */
    @Override
    public List<PointsDTO> getTopUsers(int topN) {
        if (topN <= 0) {
            throw new IllegalArgumentException("Number of top users must be positive");
        }

        return pointsMap.values().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getPoints(), p1.getPoints()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Get all users with points with pagination
     */
    @Override
    public Page<PointsDTO> getAllUsersWithPoints(Pageable pageable) {
        List<PointsDTO> allPoints = new ArrayList<>(pointsMap.values());

        // Sort by points descending by default
        allPoints.sort((p1, p2) -> Integer.compare(p2.getPoints(), p1.getPoints()));

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allPoints.size());

        if (start > allPoints.size()) {
            return Page.empty(pageable);
        }

        List<PointsDTO> pageContent = allPoints.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allPoints.size());
    }

    /**
     * Award bonus points to a user
     */
    @Override
    public void awardBonusPoints(Long userId, int bonus) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (bonus <= 0) {
            throw new IllegalArgumentException("Bonus amount must be positive");
        }

        PointsDTO points = pointsMap.get(userId);
        if (points == null) {
            points = new PointsDTO(userId, 0, LocalDateTime.now(), "Bronze", true);
            pointsMap.put(userId, points);
        }

        int oldPoints = points.getPoints();
        int newPoints = Math.min(oldPoints + bonus, maximumPointsCap);

        // Record history
        recordPointsHistory(userId, oldPoints, newPoints, "BONUS");

        // Update points
        points.setPoints(newPoints);
        points.setLastUpdated(LocalDateTime.now());
        points.setTierName(calculateTierName(newPoints));

        logger.info("Awarded {} bonus points to user {}", bonus, userId);
    }

    /**
     * Apply points penalty to a user
     */
    @Override
    public void applyPointsPenalty(Long userId, int penalty) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (penalty <= 0) {
            throw new IllegalArgumentException("Penalty amount must be positive");
        }

        PointsDTO points = pointsMap.get(userId);
        if (points != null) {
            int oldPoints = points.getPoints();
            int newPoints = Math.max(oldPoints - penalty, minimumPointsThreshold);

            // Record history
            recordPointsHistory(userId, oldPoints, newPoints, "PENALTY");

            // Update points
            points.setPoints(newPoints);
            points.setLastUpdated(LocalDateTime.now());
            points.setTierName(calculateTierName(newPoints));

            logger.info("Applied {} point penalty to user {}", penalty, userId);
        }
    }

    /**
     * Set minimum points threshold for all users
     */
    @Override
    public void setMinimumPointsThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }

        int oldThreshold = this.minimumPointsThreshold;
        this.minimumPointsThreshold = threshold;

        // Apply threshold to all users
        for (PointsDTO points : pointsMap.values()) {
            if (points.getPoints() < threshold) {
                int oldPoints = points.getPoints();

                // Record history
                recordPointsHistory(points.getUserId(), oldPoints, threshold, "MIN_THRESHOLD");

                // Update points
                points.setPoints(threshold);
                points.setLastUpdated(LocalDateTime.now());
                points.setTierName(calculateTierName(threshold));
            }
        }

        logger.info("Set minimum points threshold from {} to {}", oldThreshold, threshold);
    }

    /**
     * Set maximum points cap for all users
     */
    @Override
    public void setMaximumPointsCap(int cap) {
        if (cap <= 0) {
            throw new IllegalArgumentException("Cap must be positive");
        }

        int oldCap = this.maximumPointsCap;
        this.maximumPointsCap = cap;

        // Apply cap to all users
        for (PointsDTO points : pointsMap.values()) {
            if (points.getPoints() > cap) {
                int oldPoints = points.getPoints();

                // Record history
                recordPointsHistory(points.getUserId(), oldPoints, cap, "MAX_CAP");

                // Update points
                points.setPoints(cap);
                points.setLastUpdated(LocalDateTime.now());
                points.setTierName(calculateTierName(cap));
            }
        }

        logger.info("Set maximum points cap from {} to {}", oldCap, cap);
    }

    /**
     * Get users with points in specified range
     */
    @Override
    public List<PointsDTO> getUsersInPointsRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value");
        }

        return pointsMap.values().stream()
                .filter(p -> p.getPoints() >= min && p.getPoints() <= max)
                .collect(Collectors.toList());
    }

    /**
     * Reset all users' points
     */
    @Override
    public void resetAllPoints() {
        for (Long userId : new ArrayList<>(pointsMap.keySet())) {
            resetPoints(userId);
        }

        logger.info("Reset points for all users");
    }

    /**
     * Increase all users' points by percentage
     */
    @Override
    public void increaseAllPointsByPercentage(double percentage) {
        if (percentage <= 0) {
            throw new IllegalArgumentException("Percentage must be positive");
        }

        double factor = 1 + (percentage / 100.0);

        for (PointsDTO points : pointsMap.values()) {
            int oldPoints = points.getPoints();
            int newPoints = (int) Math.min(oldPoints * factor, maximumPointsCap);

            // Record history
            recordPointsHistory(points.getUserId(), oldPoints, newPoints, "PERCENTAGE_INCREASE");

            // Update points
            points.setPoints(newPoints);
            points.setLastUpdated(LocalDateTime.now());
            points.setTierName(calculateTierName(newPoints));
        }

        logger.info("Increased all points by {}%", percentage);
    }

    /**
     * Decrease all users' points by percentage
     */
    @Override
    public void decreaseAllPointsByPercentage(double percentage) {
        if (percentage <= 0) {
            throw new IllegalArgumentException("Percentage must be positive");
        }

        double factor = 1 - (percentage / 100.0);

        for (PointsDTO points : pointsMap.values()) {
            int oldPoints = points.getPoints();
            int newPoints = (int) Math.max(oldPoints * factor, minimumPointsThreshold);

            // Record history
            recordPointsHistory(points.getUserId(), oldPoints, newPoints, "PERCENTAGE_DECREASE");

            // Update points
            points.setPoints(newPoints);
            points.setLastUpdated(LocalDateTime.now());
            points.setTierName(calculateTierName(newPoints));
        }

        logger.info("Decreased all points by {}%", percentage);
    }

    /**
     * Get statistics about points distribution
     */
    @Override
    public PointsStatisticsDTO getPointsStatistics() {
        int totalUsers = pointsMap.size();
        int totalPoints = getTotalPoints();

        double averagePoints = totalUsers > 0 ? (double) totalPoints / totalUsers : 0;

        int maxPoints = 0;
        int minPoints = Integer.MAX_VALUE;
        int usersWithZeroPoints = 0;

        List<Integer> allPoints = new ArrayList<>();

        for (PointsDTO points : pointsMap.values()) {
            int userPoints = points.getPoints();
            allPoints.add(userPoints);

            if (userPoints > maxPoints) {
                maxPoints = userPoints;
            }

            if (userPoints < minPoints) {
                minPoints = userPoints;
            }

            if (userPoints == 0) {
                usersWithZeroPoints++;
            }
        }

        // Calculate median
        int medianPoints = 0;
        if (!allPoints.isEmpty()) {
            Collections.sort(allPoints);
            int middle = allPoints.size() / 2;
            if (allPoints.size() % 2 == 0) {
                medianPoints = (allPoints.get(middle - 1) + allPoints.get(middle)) / 2;
            } else {
                medianPoints = allPoints.get(middle);
            }
        }

        // If no users, set min to 0
        if (minPoints == Integer.MAX_VALUE) {
            minPoints = 0;
        }

        return new PointsStatisticsDTO(
                totalUsers,
                totalPoints,
                averagePoints,
                medianPoints,
                maxPoints,
                minPoints,
                usersWithZeroPoints
        );
    }

    /**
     * Convert points to rewards
     */
    @Override
    public void convertPointsToRewards(Long userId, int pointsToConvert) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (pointsToConvert <= 0) {
            throw new IllegalArgumentException("Points to convert must be positive");
        }

        PointsDTO points = pointsMap.get(userId);
        if (points == null || points.getPoints() < pointsToConvert) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Insufficient points for conversion");
        }

        int oldPoints = points.getPoints();
        int newPoints = oldPoints - pointsToConvert;

        // Record history
        recordPointsHistory(userId, oldPoints, newPoints, "CONVERT_TO_REWARDS");

        // Update points
        points.setPoints(newPoints);
        points.setLastUpdated(LocalDateTime.now());
        points.setTierName(calculateTierName(newPoints));

        logger.info("Converted {} points to rewards for user {}", pointsToConvert, userId);
    }

    /**
     * Get user's points history
     */
    @Override
    public List<PointsDTO> getUserPointsHistory(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // For simplicity, we'll just return a list with the current points
        // In a real implementation, this would query from pointsHistoryMap
        PointsDTO currentPoints = pointsMap.get(userId);
        if (currentPoints == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(currentPoints);
    }

    /**
     * Expire points older than specified days
     */
    @Override
    public void expirePoints(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }

        LocalDateTime expirationDate = LocalDateTime.now().minusDays(days);

        for (PointsDTO points : pointsMap.values()) {
            if (points.getLastUpdated().isBefore(expirationDate)) {
                int oldPoints = points.getPoints();

                // Record history
                recordPointsHistory(points.getUserId(), oldPoints, 0, "EXPIRE");

                // Reset points
                points.setPoints(0);
                points.setLastUpdated(LocalDateTime.now());
                points.setTierName(calculateTierName(0));
            }
        }

        logger.info("Expired points older than {} days", days);
    }

    /**
     * Calculate user tier based on points
     */
    @Override
    public int calculateUserTier(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        PointsDTO points = pointsMap.get(userId);
        if (points == null) {
            return 0; // Default tier for non-existent users
        }

        int userPoints = points.getPoints();
        int tier = 0;

        // Find the highest tier the user qualifies for
        for (Map.Entry<Integer, String> entry : tierMap.entrySet()) {
            if (userPoints >= entry.getKey()) {
                tier++;
            } else {
                break;
            }
        }

        return tier;
    }

    /**
     * Export all points data
     */
    @Override
    public Map<Long, PointsDTO> exportAllPointsData() {
        return new HashMap<>(pointsMap);
    }

    /**
     * Import points data
     */
    @Override
    public void importPointsData(Map<Long, PointsDTO> pointsData) {
        if (pointsData == null) {
            throw new IllegalArgumentException("Points data cannot be null");
        }

        // Clear existing data
        pointsMap.clear();

        // Import new data
        for (Map.Entry<Long, PointsDTO> entry : pointsData.entrySet()) {
            PointsDTO pointsDTO = entry.getValue();
            pointsDTO.setLastUpdated(LocalDateTime.now());
            pointsDTO.setTierName(calculateTierName(pointsDTO.getPoints()));
            pointsMap.put(entry.getKey(), pointsDTO);
        }

        logger.info("Imported points data for {} users", pointsData.size());
    }

    /**
     * Apply seasonal bonus to all users
     */
    @Override
    public void applySeasonalBonus(String seasonName, double multiplier) {
        if (seasonName == null || seasonName.trim().isEmpty()) {
            throw new IllegalArgumentException("Season name cannot be null or empty");
        }

        if (multiplier <= 0) {
            throw new IllegalArgumentException("Multiplier must be positive");
        }

        for (PointsDTO points : pointsMap.values()) {
            int oldPoints = points.getPoints();
            int newPoints = (int) Math.min(oldPoints * multiplier, maximumPointsCap);

            // Record history
            recordPointsHistory(points.getUserId(), oldPoints, newPoints, "SEASONAL_BONUS_" + seasonName.toUpperCase());

            // Update points
            points.setPoints(newPoints);
            points.setLastUpdated(LocalDateTime.now());
            points.setTierName(calculateTierName(newPoints));
        }

        logger.info("Applied seasonal bonus for {} with multiplier {}", seasonName, multiplier);
    }

    /**
     * Get leaderboard for specific time period
     */
    @Override
    public List<PointsDTO> getLeaderboardForPeriod(String startDate, String endDate, int limit) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }

        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }

        try {
            LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);

            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }

            String periodKey = startDate + "_" + endDate;

            // In a real implementation, this would query a database
            // For simplicity, we'll filter the current users by last updated date
            List<PointsDTO> periodLeaderboard = pointsMap.values().stream()
                    .filter(p -> !p.getLastUpdated().isBefore(start) && !p.getLastUpdated().isAfter(end))
                    .sorted((p1, p2) -> Integer.compare(p2.getPoints(), p1.getPoints()))
                    .limit(limit)
                    .collect(Collectors.toList());

            // Cache leaderboard for this period
            periodLeaderboardMap.put(periodKey, new ArrayList<>(periodLeaderboard));

            return periodLeaderboard;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO date-time format", e);
        }
    }

    /**
     * Calculate points needed to reach next tier
     */
    @Override
    public int calculatePointsForNextTier(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        PointsDTO points = pointsMap.get(userId);
        if (points == null) {
            // For a new user, points needed to reach first tier
            return tierMap.keySet().stream().filter(k -> k > 0).min(Integer::compare).orElse(1000);
        }

        int userPoints = points.getPoints();

        // Find the next tier threshold
        Optional<Integer> nextThreshold = tierMap.keySet().stream()
                .filter(threshold -> threshold > userPoints)
                .min(Integer::compare);

        // User is at the highest tier
        return nextThreshold.map(integer -> integer - userPoints).orElse(0);
    }

    /**
     * Merge points from multiple users into one
     */
    @Override
    public void mergeUserPoints(Long targetUserId, List<Long> sourceUserIds) {
        if (targetUserId == null || sourceUserIds == null || sourceUserIds.isEmpty()) {
            throw new IllegalArgumentException("Invalid target or source user IDs");
        }

        if (sourceUserIds.contains(targetUserId)) {
            throw new IllegalArgumentException("Target user cannot be in the source users list");
        }

        // Create target user if it doesn't exist
        PointsDTO targetPoints = pointsMap.get(targetUserId);
        if (targetPoints == null) {
            targetPoints = new PointsDTO(targetUserId, 0, LocalDateTime.now(), "Bronze", true);
            pointsMap.put(targetUserId, targetPoints);
        }

        int oldTargetPoints = targetPoints.getPoints();
        int totalMergedPoints = oldTargetPoints;

        // Merge points from all source users
        for (Long sourceUserId : sourceUserIds) {
            PointsDTO sourcePoints = pointsMap.get(sourceUserId);
            if (sourcePoints != null) {
                totalMergedPoints += sourcePoints.getPoints();

                // Record history for source
                recordPointsHistory(sourceUserId, sourcePoints.getPoints(), 0, "MERGED_TO_" + targetUserId);

                // Remove source
                pointsMap.remove(sourceUserId);
            }
        }

        // Apply maximum cap
        int newTargetPoints = Math.min(totalMergedPoints, maximumPointsCap);

        // Record history for target
        recordPointsHistory(targetUserId, oldTargetPoints, newTargetPoints, "MERGED_FROM_MULTIPLE");

        // Update target points
        targetPoints.setPoints(newTargetPoints);
        targetPoints.setLastUpdated(LocalDateTime.now());
        targetPoints.setTierName(calculateTierName(newTargetPoints));

        logger.info("Merged points from {} users into user {}", sourceUserIds.size(), targetUserId);
    }

    /**
     * Split user points to multiple users
     */
    @Override
    public void splitUserPoints(Long sourceUserId, List<Long> targetUserIds, List<Double> distribution) {
        if (sourceUserId == null || targetUserIds == null || targetUserIds.isEmpty()
                || distribution == null || distribution.isEmpty()) {
            throw new IllegalArgumentException("Invalid source, target, or distribution parameters");
        }

        if (targetUserIds.size() != distribution.size()) {
            throw new IllegalArgumentException("Target users and distribution lists must have the same size");
        }

        if (targetUserIds.contains(sourceUserId)) {
            throw new IllegalArgumentException("Source user cannot be in the target users list");
        }

        // Check if distribution sums to approximately 100%
        double totalDistribution = distribution.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalDistribution - 100.0) > 0.1) {
            throw new IllegalArgumentException("Distribution percentages must sum to 100%");
        }

        PointsDTO sourcePoints = pointsMap.get(sourceUserId);
        if (sourcePoints == null || sourcePoints.getPoints() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source user has no points to split");
        }

        int totalSourcePoints = sourcePoints.getPoints();
        int pointsDistributed = 0;

        // Split points among target users
        for (int i = 0; i < targetUserIds.size(); i++) {
            Long targetUserId = targetUserIds.get(i);
            double percentage = distribution.get(i);
            // Calculate points for this target
            int pointsForTarget = (int) Math.round((totalSourcePoints * percentage) / 100.0);

            // Ensure we don't distribute more than available due to rounding errors
            if (pointsDistributed + pointsForTarget > totalSourcePoints) {
                pointsForTarget = totalSourcePoints - pointsDistributed;
            }

            pointsDistributed += pointsForTarget;

            // Create or update target user
            PointsDTO targetPoints = pointsMap.get(targetUserId);
            if (targetPoints == null) {
                targetPoints = new PointsDTO(targetUserId, 0, LocalDateTime.now(), "Bronze", true);
                pointsMap.put(targetUserId, targetPoints);
            }

            int oldTargetPoints = targetPoints.getPoints();
            int newTargetPoints = Math.min(oldTargetPoints + pointsForTarget, maximumPointsCap);

            // Record history for target
            recordPointsHistory(targetUserId, oldTargetPoints, newTargetPoints, "SPLIT_FROM_" + sourceUserId);

            // Update target points
            targetPoints.setPoints(newTargetPoints);
            targetPoints.setLastUpdated(LocalDateTime.now());
            targetPoints.setTierName(calculateTierName(newTargetPoints));
        }

        // Record history for source
        recordPointsHistory(sourceUserId, totalSourcePoints, 0, "SPLIT_TO_MULTIPLE");

        // Reset source points
        resetPoints(sourceUserId);

        logger.info("Split {} points from user {} to {} users", totalSourcePoints, sourceUserId, targetUserIds.size());
    }

    /**
     * Get users below minimum threshold
     */
    @Override
    public List<PointsDTO> getUsersBelowThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }

        return pointsMap.values().stream()
                .filter(p -> p.getPoints() < threshold)
                .collect(Collectors.toList());
    }

    /**
     * Get users above maximum threshold
     */
    @Override
    public List<PointsDTO> getUsersAboveThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }

        return pointsMap.values().stream()
                .filter(p -> p.getPoints() > threshold)
                .collect(Collectors.toList());
    }

    /**
     * Apply points adjustment based on user activity
     */
    @Override
    public void adjustPointsByActivity(Long userId, int activityLevel) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (activityLevel < 1 || activityLevel > 10) {
            throw new IllegalArgumentException("Activity level must be between 1 and 10");
        }

        PointsDTO points = pointsMap.get(userId);
        if (points == null) {
            points = new PointsDTO(userId, 0, LocalDateTime.now(), "Bronze", true);
            pointsMap.put(userId, points);
        }

        // Calculate adjustment factor based on activity level (1-10)
        // Lower activity level: penalty, higher activity level: bonus
        double adjustmentFactor = 0.9 + (activityLevel * 0.02); // Ranges from 0.92 to 1.1

        int oldPoints = points.getPoints();
        int newPoints;

        if (adjustmentFactor >= 1.0) {
            // Bonus for high activity
            newPoints = Math.min((int)(oldPoints * adjustmentFactor), maximumPointsCap);
            recordPointsHistory(userId, oldPoints, newPoints, "ACTIVITY_BONUS");
        } else {
            // Penalty for low activity
            newPoints = Math.max((int)(oldPoints * adjustmentFactor), minimumPointsThreshold);
            recordPointsHistory(userId, oldPoints, newPoints, "ACTIVITY_PENALTY");
        }

        // Update points
        points.setPoints(newPoints);
        points.setLastUpdated(LocalDateTime.now());
        points.setTierName(calculateTierName(newPoints));

        logger.info("Adjusted points for user {} based on activity level {}", userId, activityLevel);
    }

    /**
     * Get average points per user
     */
    @Override
    public double getAveragePointsPerUser() {
        if (pointsMap.isEmpty()) {
            return 0.0;
        }

        int totalPoints = getTotalPoints();
        return (double) totalPoints / pointsMap.size();
    }

    /**
     * Get median points value
     */
    @Override
    public int getMedianPointsValue() {
        if (pointsMap.isEmpty()) {
            return 0;
        }

        List<Integer> allPoints = pointsMap.values().stream()
                .map(PointsDTO::getPoints)
                .sorted()
                .toList();

        int size = allPoints.size();
        if (size % 2 == 0) {
            // Even number of elements, average the middle two
            return (allPoints.get(size/2 - 1) + allPoints.get(size/2)) / 2;
        } else {
            // Odd number of elements, return the middle one
            return allPoints.get(size/2);
        }
    }

    /**
     * Calculate tier name based on points
     * Private helper method to determine the user's tier name
     */
    private String calculateTierName(int points) {
        String tierName = "Bronze"; // Default tier

        // Find the highest tier the user qualifies for
        for (Map.Entry<Integer, String> entry : tierMap.entrySet()) {
            if (points >= entry.getKey()) {
                tierName = entry.getValue();
            } else {
                break;
            }
        }

        return tierName;
    }

    /**
     * Record points history entry
     * Private helper method to track points history
     */
    private void recordPointsHistory(Long userId, int oldPoints, int newPoints, String action) {
        if (userId == null) {
            return;
        }

        // Create history entry
        PointsHistoryEntryDTO historyEntry = new PointsHistoryEntryDTO(
                userId,
                oldPoints,
                newPoints,
                action,
                LocalDateTime.now()
        );

        // Get or create history list for this user
        List<PointsHistoryEntryDTO> userHistory = pointsHistoryMap.computeIfAbsent(
                userId, k -> new ArrayList<>());

        // Add entry to history
        userHistory.add(historyEntry);

        // Trim history if it's getting too large (keep last 100 entries)
        if (userHistory.size() > 100) {
            userHistory = userHistory.subList(userHistory.size() - 100, userHistory.size());
            pointsHistoryMap.put(userId, userHistory);
        }

        logger.debug("Recorded points history for user {}: {} -> {} ({})",
                userId, oldPoints, newPoints, action);
    }
}