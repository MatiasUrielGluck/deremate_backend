package com.matiasugluck.deremate_backend.dto;

import com.matiasugluck.deremate_backend.entity.Route;

import java.time.LocalDateTime;

public class RouteDTO {

    private Long id;
    private String origin;
    private String destination;
    private String packageLocation;
    private String status;
    private String qrCode;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String assignedToEmail;

    // Constructor
    public RouteDTO(Route route) {
        this.id = route.getId();
        this.origin = route.getOrigin();
        this.destination = route.getDestination();
        this.packageLocation = route.getPackageLocation();
        this.status = route.getStatus();
        this.qrCode = route.getQrCode();
        this.createdAt = route.getCreatedAt();
        this.completedAt = route.getCompletedAt();
        this.assignedToEmail = route.getAssignedTo() != null ? route.getAssignedTo().getEmail() : null;
    }

    // Getters y Setters
}
