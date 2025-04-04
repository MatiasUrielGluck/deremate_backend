package com.matiasugluck.deremate_backend.service;

import com.matiasugluck.deremate_backend.dto.delivery.CreateDeliveryDTO;
import com.matiasugluck.deremate_backend.dto.delivery.DeliveryDTO;

public interface DeliveryService {
    DeliveryDTO createDelivery(CreateDeliveryDTO createDeliveryDTO);
    void confirmDelivery(Long id, String pin);
    void cancelDelivery(Long id);
    DeliveryDTO getDeliveryById(Long id);
}
