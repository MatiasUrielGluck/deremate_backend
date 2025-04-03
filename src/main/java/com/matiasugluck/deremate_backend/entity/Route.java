package com.matiasugluck.deremate_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity

public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origin;
    private String destination;
    private String packageLocation; // ej: Estante A2
    private String status = "pendiente"; // pendiente, en_curso, completada
    private LocalDateTime completedAt;


    private LocalDateTime createdAt = LocalDateTime.now();
    @JsonIgnoreProperties({"authorities", "password", "enabled"})
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User assignedTo;

    @Column(unique = true)
    private String qrCode; // Este es el string que representa el QR



    // Getters y Setters
}
