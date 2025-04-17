package com.matiasugluck.deremate_backend.dto.points;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RewardSpinDTO {
    private String rewardName;
    private int rewardPoints;
    private boolean jackpot;
    private String message;
}