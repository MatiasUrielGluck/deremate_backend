package com.matiasugluck.deremate_backend.dto.points;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPointsDTO {
    private Long userId;
    private int points;
    private int level;
    private int pointsToNextLevel;
    private String tier;
    private double progressPercentage;




}





















