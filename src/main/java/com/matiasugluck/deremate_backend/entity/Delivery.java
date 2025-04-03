package com.matiasugluck.deremate_backend.entity;

import com.matiasugluck.deremate_backend.dto.delivery.DeliveryDTO;
import com.matiasugluck.deremate_backend.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
                .user(user.toDTO())
                .route(route.toDto())
                .products(products.stream().map(Product::toDto).toList())
                .build();
    }
}
