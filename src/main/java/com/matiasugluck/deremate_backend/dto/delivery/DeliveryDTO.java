package com.matiasugluck.deremate_backend.dto.delivery;

import com.matiasugluck.deremate_backend.dto.RouteDTO;
import com.matiasugluck.deremate_backend.dto.product.ProductDTO;
import com.matiasugluck.deremate_backend.enums.DeliveryStatus;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
public class DeliveryDTO {
    private Long id;
    private DeliveryStatus status;
    private String destination;
    private String packageLocation;
    private Timestamp createdDate;
    private Timestamp deliveryStartDate;
    private Timestamp deliveryEndDate;
    private RouteDTO route;
    private List<ProductDTO> products;
    private String qrCode;
    private String pin;
}
