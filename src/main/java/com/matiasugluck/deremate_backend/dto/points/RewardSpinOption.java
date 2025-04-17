package com.matiasugluck.deremate_backend.dto.points;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RewardSpinOption {
    private String name;
    private int points;
    private boolean jackpot;
}
