package com.matiasugluck.deremate_backend.service;

import com.matiasugluck.deremate_backend.dto.RouteDTO;
import com.matiasugluck.deremate_backend.dto.delivery.DeliveryDTO;

import java.util.List;

public interface DeliveryService {
    void initDelivery(RouteDTO route);
    void confirmDelivery(Long id);
    void cancelDelivery(Long id);
    DeliveryDTO getDeliveryById(Long id);
    List<DeliveryDTO> getDeliveriesByUserId(Long id);
}
