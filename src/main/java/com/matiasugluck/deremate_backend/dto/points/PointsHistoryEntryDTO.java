package com.matiasugluck.deremate_backend.dto.points;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsHistoryEntryDTO {
    private Long userId;
    private int previousPoints;
    private int newPoints;
    private String operation;
    private LocalDateTime timestamp;
}