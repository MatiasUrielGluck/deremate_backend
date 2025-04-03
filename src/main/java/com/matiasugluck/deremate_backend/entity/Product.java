package com.matiasugluck.deremate_backend.entity;

import com.matiasugluck.deremate_backend.dto.product.ProductDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private BigDecimal price;

    public ProductDTO toDto() {
        return ProductDTO.builder()
                .id(id)
                .name(name)
                .description(description)
                .price(price)
                .build();
    }
}
