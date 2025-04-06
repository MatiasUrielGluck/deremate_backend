package com.matiasugluck.deremate_backend.dto.route;

import com.matiasugluck.deremate_backend.enums.RouteStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvailableRouteDTO {
    private Long id;
    private String origin;
    private String destination;
    private RouteStatus status;
}
