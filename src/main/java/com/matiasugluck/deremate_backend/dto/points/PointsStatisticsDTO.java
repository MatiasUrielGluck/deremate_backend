package com.matiasugluck.deremate_backend.dto.points;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsStatisticsDTO {
    private int totalUsers;
    private int totalPoints;
    private double averagePoints;
    private int medianPoints;
    private int maxPoints;
    private int minPoints;
    private int usersWithZeroPoints;
}
