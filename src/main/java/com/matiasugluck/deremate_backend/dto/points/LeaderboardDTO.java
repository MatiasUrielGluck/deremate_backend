package com.matiasugluck.deremate_backend.dto.points;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardDTO {
    private String periodName;
    private String startDate;
    private String endDate;
    private List<PointsDTO> topUsers;
}