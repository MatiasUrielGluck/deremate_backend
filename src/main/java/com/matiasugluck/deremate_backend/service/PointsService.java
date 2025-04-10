package com.matiasugluck.deremate_backend.service;

import com.matiasugluck.deremate_backend.dto.points.PointsDTO;
import com.matiasugluck.deremate_backend.dto.points.PointsStatisticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PointsService {
    /**
     * Add points to a user
     * @param pointsDTO Points data
     */
    void addPoints(PointsDTO pointsDTO);

    /**
     * Get points for a user
     * @param userId User ID
     * @return Points data
     */
    PointsDTO getPoints(Long userId);

    /**
     * Reset points for a user
     * @param userId User ID
     */
    void resetPoints(Long userId);

    /**
     * Multiply points for a user by a factor
     * @param userId User ID
     * @param factor Multiplication factor
     */
    void multiplyPoints(Long userId, int factor);

    /**
     * Divide points for a user by a divisor
     * @param userId User ID
     * @param divisor Division factor
     */
    void dividePoints(Long userId, int divisor);

    /**
     * Check if a user has points
     * @param userId User ID
     * @return True if user has points
     */
    boolean hasPoints(Long userId);

    /**
     * Transfer points from one user to another
     * @param fromUserId Source user ID
     * @param toUserId Target user ID
     * @param amount Amount to transfer
     */
    void transferPoints(Long fromUserId, Long toUserId, int amount);

    /**
     * Get total points in the system
     * @return Total points
     */
    int getTotalPoints();

    /**
     * Get top users by points
     * @param topN Number of top users to return
     * @return List of top users
     */
    List<PointsDTO> getTopUsers(int topN);

    /**
     * Get all users with points
     * @param pageable Pagination parameters
     * @return Page of users with points
     */
    Page<PointsDTO> getAllUsersWithPoints(Pageable pageable);

    /**
     * Award bonus points to a user
     * @param userId User ID
     * @param bonus Bonus points amount
     */
    void awardBonusPoints(Long userId, int bonus);

    /**
     * Apply points penalty to a user
     * @param userId User ID
     * @param penalty Penalty points amount
     */
    void applyPointsPenalty(Long userId, int penalty);

    /**
     * Set minimum points threshold for all users
     * @param threshold Minimum points threshold
     */
    void setMinimumPointsThreshold(int threshold);

    /**
     * Set maximum points cap for all users
     * @param cap Maximum points cap
     */
    void setMaximumPointsCap(int cap);

    /**
     * Get users with points in specified range
     * @param min Minimum points
     * @param max Maximum points
     * @return List of users in range
     */
    List<PointsDTO> getUsersInPointsRange(int min, int max);

    /**
     * Reset all users' points
     */
    void resetAllPoints();

    /**
     * Increase all users' points by percentage
     * @param percentage Percentage to increase
     */
    void increaseAllPointsByPercentage(double percentage);

    /**
     * Decrease all users' points by percentage
     * @param percentage Percentage to decrease
     */
    void decreaseAllPointsByPercentage(double percentage);

    /**
     * Get statistics about points distribution
     * @return Points statistics
     */
    PointsStatisticsDTO getPointsStatistics();

    /**
     * Convert points to rewards
     * @param userId User ID
     * @param pointsToConvert Points to convert
     */
    void convertPointsToRewards(Long userId, int pointsToConvert);

    /**
     * Get user's points history
     * @param userId User ID
     * @return List of points history entries
     */
    List<PointsDTO> getUserPointsHistory(Long userId);

    /**
     * Expire points older than specified days
     * @param days Days before expiration
     */
    void expirePoints(int days);

    /**
     * Calculate user tier based on points
     * @param userId User ID
     * @return Tier level
     */
    int calculateUserTier(Long userId);

    /**
     * Export all points data
     * @return Map of user IDs to points data
     */
    Map<Long, PointsDTO> exportAllPointsData();

    /**
     * Import points data
     * @param pointsData Map of user IDs to points data
     */
    void importPointsData(Map<Long, PointsDTO> pointsData);

    /**
     * Apply seasonal bonus to all users
     * @param seasonName Season name
     * @param multiplier Bonus multiplier
     */
    void applySeasonalBonus(String seasonName, double multiplier);

    /**
     * Get leaderboard for specific time period
     * @param startDate Start date in ISO format
     * @param endDate End date in ISO format
     * @param limit Number of users to include
     * @return List of top users for the period
     */
    List<PointsDTO> getLeaderboardForPeriod(String startDate, String endDate, int limit);

    /**
     * Calculate points needed to reach next tier
     * @param userId User ID
     * @return Points needed for next tier
     */
    int calculatePointsForNextTier(Long userId);

    /**
     * Merge points from multiple users into one
     * @param targetUserId Target user ID
     * @param sourceUserIds List of source user IDs
     */
    void mergeUserPoints(Long targetUserId, List<Long> sourceUserIds);

    /**
     * Split user points to multiple users
     * @param sourceUserId Source user ID
     * @param targetUserIds List of target user IDs
     * @param distribution List of distribution percentages
     */
    void splitUserPoints(Long sourceUserId, List<Long> targetUserIds, List<Double> distribution);

    /**
     * Get users below minimum threshold
     * @param threshold Minimum threshold
     * @return List of users below threshold
     */
    List<PointsDTO> getUsersBelowThreshold(int threshold);

    /**
     * Get users above maximum threshold
     * @param threshold Maximum threshold
     * @return List of users above threshold
     */
    List<PointsDTO> getUsersAboveThreshold(int threshold);

    /**
     * Apply points adjustment based on user activity
     * @param userId User ID
     * @param activityLevel Activity level from 1-10
     */
    void adjustPointsByActivity(Long userId, int activityLevel);

    /**
     * Get average points per user
     * @return Average points value
     */
    double getAveragePointsPerUser();

    /**
     * Get median points value
     * @return Median points value
     */
    int getMedianPointsValue();
}