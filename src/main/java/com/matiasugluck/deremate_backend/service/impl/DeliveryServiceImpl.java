package com.matiasugluck.deremate_backend.service.impl;

import com.google.firebase.messaging.Notification;
import com.matiasugluck.deremate_backend.constants.DeliveryApiMessages;
import com.matiasugluck.deremate_backend.dto.delivery.CreateDeliveryDTO;
import com.matiasugluck.deremate_backend.dto.delivery.DeliveryDTO;
import com.matiasugluck.deremate_backend.dto.delivery.PackageInWarehouseDTO;
import com.matiasugluck.deremate_backend.entity.*;
import com.matiasugluck.deremate_backend.enums.DeliveryStatus;
import com.matiasugluck.deremate_backend.enums.RouteStatus;
import com.matiasugluck.deremate_backend.exception.ApiException;
import com.matiasugluck.deremate_backend.graphql.input.DeliveryFilterInput;
import com.matiasugluck.deremate_backend.repository.DeliveryRepository;
import com.matiasugluck.deremate_backend.repository.DeviceRepository;
import com.matiasugluck.deremate_backend.repository.ProductRepository;
import com.matiasugluck.deremate_backend.repository.RouteRepository;
import com.matiasugluck.deremate_backend.service.DeliveryService;
import com.matiasugluck.deremate_backend.service.FirebaseNotificationService;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final ProductRepository productRepository;
    private final RouteRepository routeRepository;
    private final FirebaseNotificationService firebaseNotificationService;
    private final DeviceRepository deviceRepository;

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
                    .destination(new Coordinates(createDeliveryDTO.getDestinationLatitude(),createDeliveryDTO.getDestinationLongitude()))
                    .status(RouteStatus.PENDING)
                    .build();

            Route savedRoute = routeRepository.save(route);

            Delivery delivery = Delivery.builder()
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


            List<Device> allDevices = deviceRepository.findAll();


            for (Device device : allDevices) {
                NotificationMessage notification = NotificationMessage.builder()
                        .title("Nueva entrega disponible")
                        .body("Hay un nuevo paquete en " + resultDelivery.getPackageLocation() + " listo para ser retirado.")
                        .recipientToken(device.getDeviceId())
                        .build();
                firebaseNotificationService.sendNotification(notification);
            }


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
        delivery.getRoute().setStatus(RouteStatus.COMPLETED);
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.getRoute().setCompletedAt(Timestamp.from(Instant.now()));
        deliveryRepository.save(delivery);
    }

    @Override
    public void cancelDelivery(Long id) {
        Delivery delivery = findDeliveryById(id);
        delivery.setStatus(DeliveryStatus.REJECTED);
        Route route = delivery.getRoute();
        if (route != null) {
            // --- Notification Logic Starts ---
            User assignedUser = route.getAssignedTo();
            if (assignedUser != null) {
                // Find all linked devices for this user
                List<Device> userDevices = deviceRepository.findByUser(assignedUser);

                // Send a notification to each linked device
                for (Device device : userDevices) {
                    NotificationMessage notification = NotificationMessage.builder()
                            .title("Entrega Cancelada")
                            .body("La entrega #" + delivery.getId() +" con destino a: "+ delivery.getRoute().getDescription()+ " fue cancelada y se ha quitado de tu ruta.")
                            .recipientToken(device.getDeviceId())
                            .build();
                    firebaseNotificationService.sendNotification(notification);
                }
            }

            route.setStatus(RouteStatus.CANCELLED);
            route.setCompletedAt(Timestamp.from(Instant.now()));
            routeRepository.save(route);
        }

        deliveryRepository.save(delivery);
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
        return deliveryRepository.findUnassignedDeliveries(DeliveryStatus.NOT_DELIVERED).stream()
                .map(delivery -> new PackageInWarehouseDTO(
                        delivery.getId(),
                        delivery.getStatus().name(),
                        delivery.getPackageLocation(),
                        delivery.getCreatedDate()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryDTO> getDeliveriesByUserId(Long userId) {
        return deliveryRepository.findByUserId(userId).stream()
                .map(delivery -> {
                    DeliveryDTO dto = delivery.toDto();
                    dto.setRoute(delivery.getRoute().toDto());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryDTO> getAssignedAndNotDelivered() {
        List<Delivery> deliveries = deliveryRepository.findAssignedAndNotDelivered(DeliveryStatus.NOT_DELIVERED);
        return deliveries.stream()
                .map(delivery -> {
                    DeliveryDTO dto = delivery.toDto();
                    if (delivery.getRoute() != null) {
                        dto.setRoute(delivery.getRoute().toDto());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Delivery> findDeliveriesByFilter(DeliveryFilterInput filter) {
        if (filter == null) {
            return deliveryRepository.findAll();
        }
        return deliveryRepository.findDeliveriesByFilter(filter);
    }

}
