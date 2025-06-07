package com.matiasugluck.deremate_backend.dto.delivery;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateDeliveryDTO {
    private Double destinationLatitude;

    private Double destinationLongitude;

    private String packageLocation;

    private List<Long> productIds;
}
