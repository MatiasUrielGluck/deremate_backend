package com.matiasugluck.deremate_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "devices", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"deviceId", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String deviceId;
}
