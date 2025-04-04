package com.matiasugluck.deremate_backend.dto.delivery;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateDeliveryDTO {
    private String destination;

    private String packageLocation;

    private List<Long> productIds;
}
