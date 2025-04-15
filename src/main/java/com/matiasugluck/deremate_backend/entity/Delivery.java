package com.matiasugluck.deremate_backend.entity;

import com.matiasugluck.deremate_backend.dto.delivery.DeliveryDTO;
import com.matiasugluck.deremate_backend.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "delivery")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @Column(name = "destination")
    private String destination;

    private String packageLocation; // ej: Estante A2

    @Column(name = "created_date")
    private Timestamp createdDate;

    @Column(name = "delivery_start_date")
    private Timestamp deliveryStartDate;

    @Column(name = "delivery_end_date")
    private Timestamp deliveryEndDate;

    @Column(name = "qr_code", length = 500)
    private String qrCode; // Este es el string que representa el QR en BASE64

    @Column(name = "pin", nullable = false)
    private String pin;

    @OneToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToMany
    @JoinTable(
            name = "delivery_products",
            joinColumns = @JoinColumn(name = "delivery_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

    public DeliveryDTO toDto() {
        return DeliveryDTO.builder()
                .id(id)
                .status(status)
                .destination(destination)
                .packageLocation(packageLocation)
                .createdDate(createdDate)
                .deliveryStartDate(deliveryStartDate)
                .deliveryEndDate(deliveryEndDate)
                .products(products.stream().map(Product::toDto).toList())
                .qrCode(qrCode)
                .pin(pin)
                .route(route.toDto())
                .build();
    }
}
