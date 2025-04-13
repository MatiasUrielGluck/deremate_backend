package com.matiasugluck.deremate_backend.service.impl;

import com.matiasugluck.deremate_backend.constants.DeliveryApiMessages;
import com.matiasugluck.deremate_backend.dto.delivery.CreateDeliveryDTO;
import com.matiasugluck.deremate_backend.dto.delivery.DeliveryDTO;
import com.matiasugluck.deremate_backend.dto.delivery.PackageInWarehouseDTO;
import com.matiasugluck.deremate_backend.entity.Delivery;
import com.matiasugluck.deremate_backend.entity.Product;
import com.matiasugluck.deremate_backend.entity.Route;
import com.matiasugluck.deremate_backend.enums.DeliveryStatus;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import com.matiasugluck.deremate_backend.exception.ApiException;
import com.matiasugluck.deremate_backend.repository.DeliveryRepository;
import com.matiasugluck.deremate_backend.repository.ProductRepository;
import com.matiasugluck.deremate_backend.repository.RouteRepository;
import com.matiasugluck.deremate_backend.service.DeliveryService;
import com.matiasugluck.deremate_backend.utils.PinGenerator;
import com.matiasugluck.deremate_backend.utils.QRCodeGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final ProductRepository productRepository;
    private final RouteRepository routeRepository;

    @Override
    @Transactional
    public DeliveryDTO createDelivery(CreateDeliveryDTO createDeliveryDTO) {
        // Validate the product ids
        List<Product> products = new ArrayList<>();
        for (Long id : createDeliveryDTO.getProductIds()) {
            products.add(productRepository.findById(id).orElseThrow(() -> new ApiException(
                    DeliveryApiMessages.INVALID_PRODUCTS_CODE,
                    DeliveryApiMessages.INVALID_PRODUCTS_DESC,
                    HttpStatus.BAD_REQUEST.value()))
            );
        }

        try {
            Route route = Route.builder()
                    .origin("DeRemate - Main Facility")
                    .destination(createDeliveryDTO.getDestination())
                    .status(RouteStatus.PENDING)
                    .build();

            Route savedRoute = routeRepository.save(route);

            Delivery delivery = Delivery.builder()
                .destination(createDeliveryDTO.getDestination())
                .packageLocation(createDeliveryDTO.getPackageLocation())
                .createdDate(Timestamp.from(Instant.now()))
                .status(DeliveryStatus.NOT_DELIVERED)
                .pin(PinGenerator.generatePin())
                .products(products)
                .route(savedRoute)
                .build();

            Delivery savedDelivery = deliveryRepository.save(delivery);
            savedDelivery.setQrCode(QRCodeGenerator.generateQRCodeBase64(savedDelivery.getId()));
            Delivery resultDelivery = deliveryRepository.save(savedDelivery);

            return resultDelivery.toDto();
        } catch (Exception e) {
            throw new ApiException(
                    DeliveryApiMessages.INTERNAL_ERROR_CODE,
                    DeliveryApiMessages.INTERNAL_ERROR_DESC,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public void confirmDelivery(Long id, String pin) {
        Delivery delivery = findDeliveryById(id);
        if (!pin.equals(delivery.getPin())) {
            throw new ApiException(
                    DeliveryApiMessages.INVALID_PIN_CODE,
                    DeliveryApiMessages.INVALID_PIN_DESC,
                    HttpStatus.BAD_REQUEST.value()
            );
        }
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
        DeliveryDTO result = delivery.toDto();
        result.setRoute(delivery.getRoute().toDto());
        return delivery.toDto();
    }

    private Delivery findDeliveryById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        DeliveryApiMessages.DELIVERY_NOT_FOUND_CODE,
                        DeliveryApiMessages.DELIVERY_NOT_FOUND_DESC,
                        HttpStatus.NOT_FOUND.value())
                );
    }
    @Override
    public List<PackageInWarehouseDTO> getPackagesInWarehouse() {
        return deliveryRepository.findAll().stream()
                .filter(delivery -> delivery.getStatus() == DeliveryStatus.NOT_DELIVERED)
                .map(delivery -> new PackageInWarehouseDTO(
                        delivery.getId(),
                        delivery.getStatus().name(),
                        delivery.getPackageLocation()
                ))
                .collect(Collectors.toList());
    }

}
