package com.matiasugluck.deremate_backend.service;

import com.matiasugluck.deremate_backend.dto.points.UserPointsDTO;

public interface PointsService {
    void addPointsForCompletedDelivery(Long userId);
    UserPointsDTO getUserPointsInfo(Long userId);
    double getProgressPercentage(Long userId);
    void subtractPoints(Long userId, int points);
    void resetPoints(Long userId);
    void boostPoints(Long userId, int multiplier);
}







































