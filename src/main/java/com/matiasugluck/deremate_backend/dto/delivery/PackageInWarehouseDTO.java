package com.matiasugluck.deremate_backend.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class PackageInWarehouseDTO {
    private Long id;
    private String status;
    private String packageLocation;
    private Timestamp createdDate;
}
