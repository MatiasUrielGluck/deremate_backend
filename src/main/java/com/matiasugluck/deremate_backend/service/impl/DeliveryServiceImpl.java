package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.dto.RouteDTO;
import com.matiasugluck.deremate_backend.dto.delivery.DeliveryDTO;
import com.matiasugluck.deremate_backend.entity.Delivery;
import com.matiasugluck.deremate_backend.enums.DeliveryStatus;
import com.matiasugluck.deremate_backend.exception.ApiException;
import com.matiasugluck.deremate_backend.repository.DeliveryRepository;
import com.matiasugluck.deremate_backend.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;

    @Override
    public void initDelivery(RouteDTO route) {

    }

    @Override
    public void confirmDelivery(Long id) {
        Delivery delivery = findDeliveryById(id);
        delivery.setStatus(DeliveryStatus.DELIVERED);
    }

    @Override
    public void cancelDelivery(Long id) {
        Delivery delivery = findDeliveryById(id);
        delivery.setStatus(DeliveryStatus.REJECTED);
    }

    @Override
    public DeliveryDTO getDeliveryById(Long id) {
        Delivery delivery = findDeliveryById(id);
        return delivery.toDto();
    }

    @Override
    public List<DeliveryDTO> getDeliveriesByUserId(Long id) {
        List<Delivery> deliveries = deliveryRepository.findAllByUserId(id);
        return deliveries.stream().map(Delivery::toDto).toList();
    }

    private Delivery findDeliveryById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "DELIVERY_NOT_FOUND",
                        "Delivery not found",
                        HttpStatus.NOT_FOUND.value()));
    }
}
