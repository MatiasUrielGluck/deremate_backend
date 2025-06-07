package com.matiasugluck.deremate_backend.dto.route;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateRouteDTO {
    private String destinationLatitude;
    private String destinationLongitude;
    private Long userId;
}
