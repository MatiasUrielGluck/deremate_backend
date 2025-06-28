package com.matiasugluck.deremate_backend.entity;

import com.matiasugluck.deremate_backend.dto.RouteDTO;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import jakarta.persistence.*;

import java.sql.Timestamp;

import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "route")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude"))
    })
    private Coordinates destination;

    @Enumerated(EnumType.STRING)
    private RouteStatus status;

    @Column(name = "started_at")
    private Timestamp startedAt;

    @Column(name = "completed_at")
    private Timestamp completedAt;

    @Column(name = "last_updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Timestamp lastUpdatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User assignedTo;

    // Getters y Setters

    public RouteDTO toDto() {
        return RouteDTO.builder()
                .id(id)
                .description(description != null ? description : "")
                .destinationLatitude(destination != null ? destination.getLatitude() : null)
                .destinationLongitude(destination != null ? destination.getLongitude() : null)
                .status(status)
                .assignedToEmail(assignedTo != null ? assignedTo.getEmail() : "")
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();
    }
}
