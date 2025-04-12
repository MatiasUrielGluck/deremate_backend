package com.matiasugluck.deremate_backend.dto.points;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsDTO {
    private Long userId;
    private int points;
    private LocalDateTime lastUpdated;
    private String tierName;
    private boolean active;

    public PointsDTO(Long userId, int points) {
        this.userId = userId;
        this.points = points;
        this.lastUpdated = LocalDateTime.now();
        this.active = true;
    }
}