package com.matiasugluck.deremate_backend.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {

    private Double latitude;
    private Double longitude;

    public Coordinates(String destinationLatitude, String destinationLongitude) {
    }
}
