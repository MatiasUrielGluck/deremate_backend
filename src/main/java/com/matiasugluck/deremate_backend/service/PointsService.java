package com.matiasugluck.deremate_backend.service;

import com.matiasugluck.deremate_backend.dto.points.RewardSpinDTO;
import com.matiasugluck.deremate_backend.dto.points.UserPointsDTO;

public interface PointsService {
    void addPointsForCompletedDelivery(Long userId);
    void addPointsByDistance(Long userId, double kilometers);
    UserPointsDTO getUserPointsInfo(Long userId);
    double getProgressPercentage(Long userId);
    void subtractPoints(Long userId, int points);
    void resetPoints(Long userId);
    void boostPoints(Long userId, int multiplier);
    RewardSpinDTO spinRewardWheel(Long userId);
}







































