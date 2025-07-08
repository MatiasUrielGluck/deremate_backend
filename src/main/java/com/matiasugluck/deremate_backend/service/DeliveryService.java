package com.matiasugluck.deremate_backend.service;

import com.matiasugluck.deremate_backend.dto.delivery.CreateDeliveryDTO;
import com.matiasugluck.deremate_backend.dto.delivery.DeliveryDTO;
import com.matiasugluck.deremate_backend.dto.delivery.PackageInWarehouseDTO;
import com.matiasugluck.deremate_backend.entity.Delivery;
import com.matiasugluck.deremate_backend.graphql.input.DeliveryFilterInput;

import java.util.List;

public interface DeliveryService {
    DeliveryDTO createDelivery(CreateDeliveryDTO createDeliveryDTO);
    void confirmDelivery(Long id, String pin);
    void cancelDelivery(Long id);
    DeliveryDTO getDeliveryById(Long id);
    List<PackageInWarehouseDTO> getPackagesInWarehouse();
    List<DeliveryDTO> getDeliveriesByUserId(Long userId);
    List<DeliveryDTO> getAssignedAndNotDelivered();
    List<Delivery> findDeliveriesByFilter(DeliveryFilterInput filter);
}
