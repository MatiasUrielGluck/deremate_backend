package com.matiasugluck.deremate_backend.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PackageInWarehouseDTO {
    private Long id;
    private String status;
    private String packageLocation;
}
