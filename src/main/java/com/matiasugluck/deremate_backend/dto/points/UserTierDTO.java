package com.matiasugluck.deremate_backend.dto.points;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTierDTO {
    private Long userId;
    private int tier;
    private String tierName;
    private int currentPoints;
    private int pointsToNextTier;
    private int maxTier;
}