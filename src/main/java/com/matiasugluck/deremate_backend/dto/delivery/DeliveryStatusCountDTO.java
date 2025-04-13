package com.matiasugluck.deremate_backend.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliveryStatusCountDTO {
    private String status;
    private Long count;
}
