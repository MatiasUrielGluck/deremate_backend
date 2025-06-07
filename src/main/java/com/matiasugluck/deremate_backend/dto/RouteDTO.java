package com.matiasugluck.deremate_backend.dto;

import com.matiasugluck.deremate_backend.enums.RouteStatus;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Builder
@Data
public class RouteDTO {
    private Long id;
    private String description;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private RouteStatus status;
    private Timestamp completedAt;
    private String assignedToEmail;
}
