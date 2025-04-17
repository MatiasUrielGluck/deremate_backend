package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.dto.points.UserPointsDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.exception.ApiException;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final UserRepository userRepository;

    @Override
    public void addPointsForCompletedDelivery(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "Usuario no encontrado", 404));

        int earnedPoints = 10;
        int newPoints = user.getPoints() + earnedPoints;
        int newLevel = calculateLevel(newPoints);

        user.setPoints(newPoints);
        user.setLevel(newLevel);

        userRepository.save(user);
    }

    @Override
    public UserPointsDTO getUserPointsInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "Usuario no encontrado", 404));

        int nextLevelThreshold = getNextLevelThreshold(user.getLevel());
        int pointsToNext = Math.max(0, nextLevelThreshold - user.getPoints());

        return UserPointsDTO.builder()
                .userId(user.getId())
                .points(user.getPoints())
                .level(user.getLevel())
                .pointsToNextLevel(pointsToNext)
                .build();
    }

    private int calculateLevel(int points) {
        int level = 1;
        int threshold = 100;

        while (points >= threshold) {
            level++;
            threshold += level * 100;
        }

        return level;
    }

    private int getNextLevelThreshold(int currentLevel) {
        int threshold = 100;
        for (int i = 2; i <= currentLevel; i++) {
            threshold += i * 100;
        }
        return threshold + (currentLevel + 1) * 100;
    }
}