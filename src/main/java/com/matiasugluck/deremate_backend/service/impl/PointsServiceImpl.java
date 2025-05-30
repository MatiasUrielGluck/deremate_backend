package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.constants.PointsApiMessages;
import com.matiasugluck.deremate_backend.dto.points.RewardSpinDTO;
import com.matiasugluck.deremate_backend.dto.points.RewardSpinOption;
import com.matiasugluck.deremate_backend.dto.points.UserPointsDTO;
import com.matiasugluck.deremate_backend.entity.User;
import com.matiasugluck.deremate_backend.exception.ApiException;
import com.matiasugluck.deremate_backend.repository.UserRepository;
import com.matiasugluck.deremate_backend.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;


@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final UserRepository userRepository;

    private final List<RewardSpinOption> wheelOptions = List.of(
            new RewardSpinOption("Nada", 0, false),
            new RewardSpinOption("Pequeño premio", 5, false),
            new RewardSpinOption("Premio medio", 15, false),
            new RewardSpinOption("Gran premio", 30, false),
            new RewardSpinOption("Jackpot", 100, true)
    );

    public void addCustomPoints(Long userId, int earnedPoints) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        PointsApiMessages.USER_NOT_FOUND_CODE,
                        PointsApiMessages.USER_NOT_FOUND_DESC,
                        404
                ));

        int newPoints = user.getPoints() + earnedPoints;
        int newLevel = calculateLevel(newPoints);

        user.setPoints(newPoints);
        user.setLevel(newLevel);

        userRepository.save(user);
    }

    @Override
    public void addPointsForCompletedDelivery(Long userId) {
        addCustomPoints(userId, 10);
    }

    @Override
    public void addPointsByDistance(Long userId, double kilometers) {
        int earnedPoints = (int) Math.floor(kilometers * 2);
        addCustomPoints(userId, earnedPoints);
    }

    @Override
    public void boostPoints(Long userId, int multiplier) {
        int basePoints = 10;
        addCustomPoints(userId, basePoints * multiplier);
    }

    @Override
    public void subtractPoints(Long userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        PointsApiMessages.USER_NOT_FOUND_CODE,
                        PointsApiMessages.USER_NOT_FOUND_DESC,
                        404
                ));

        int newPoints = Math.max(0, user.getPoints() - points);
        user.setPoints(newPoints);
        user.setLevel(calculateLevel(newPoints));

        userRepository.save(user);
    }

    @Override
    public void resetPoints(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        PointsApiMessages.USER_NOT_FOUND_CODE,
                        PointsApiMessages.USER_NOT_FOUND_DESC,
                        404
                ));

        user.setPoints(0);
        user.setLevel(1);
        userRepository.save(user);
    }


    @Override
    public UserPointsDTO getUserPointsInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        PointsApiMessages.USER_NOT_FOUND_CODE,
                        PointsApiMessages.USER_NOT_FOUND_DESC,
                        404
                ));

        int nextLevelThreshold = getNextLevelThreshold(user.getLevel());
        int previousLevelThreshold = getPreviousLevelThreshold(user.getLevel());
        int pointsToNext = Math.max(0, nextLevelThreshold - user.getPoints());

        double progress = (double)(user.getPoints() - previousLevelThreshold) / (nextLevelThreshold - previousLevelThreshold);
        double progressPercentage = Math.min(100.0, Math.max(0.0, progress * 100));

        return UserPointsDTO.builder()
                .userId(user.getId())
                .points(user.getPoints())
                .level(user.getLevel())
                .pointsToNextLevel(pointsToNext)
                .tier(getTier(user.getPoints()))
                .progressPercentage(progressPercentage)
                .build();
    }

    @Override
    public double getProgressPercentage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        PointsApiMessages.USER_NOT_FOUND_CODE,
                        PointsApiMessages.USER_NOT_FOUND_DESC,
                        404
                ));

        int nextLevelThreshold = getNextLevelThreshold(user.getLevel());
        int previousLevelThreshold = getPreviousLevelThreshold(user.getLevel());

        double progress = (double)(user.getPoints() - previousLevelThreshold) / (nextLevelThreshold - previousLevelThreshold);
        return Math.min(100.0, Math.max(0.0, progress * 100));
    }

    @Override
    public RewardSpinDTO spinRewardWheel(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        PointsApiMessages.USER_NOT_FOUND_CODE,
                        PointsApiMessages.USER_NOT_FOUND_DESC,
                        404
                ));
        /*
        if (!user.isCanSpinWheel()) {
            return RewardSpinResultDTO.builder()
                    .rewardName("Bloqueado")
                    .rewardPoints(0)
                    .jackpot(false)
                    .message("Ya has girado la ruleta hoy. Inténtalo mañana.")
                    .build();
        }
        */

        Random random = new Random();
        RewardSpinOption option = wheelOptions.get(random.nextInt(wheelOptions.size()));

        addCustomPoints(userId, option.getPoints());
        // user.setCanSpinWheel(false);
        // userRepository.save(user);

        return RewardSpinDTO.builder()
                .rewardName(option.getName())
                .rewardPoints(option.getPoints())
                .jackpot(option.isJackpot())
                .message("Has ganado: " + option.getName() + " (" + option.getPoints() + " puntos)")
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

    private int getPreviousLevelThreshold(int currentLevel) {
        int threshold = 0;
        for (int i = 1; i < currentLevel; i++) {
            threshold += i * 100;
        }
        return threshold;
    }

    private String getTier(int points) {
        if (points >= 3000) return "Diamante";
        if (points >= 2000) return "Platino";
        if (points >= 1000) return "Oro";
        if (points >= 500) return "Plata";
        return "Bronce";
    }
}