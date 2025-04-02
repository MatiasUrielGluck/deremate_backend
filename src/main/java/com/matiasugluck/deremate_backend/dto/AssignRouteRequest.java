package com.matiasugluck.deremate_backend.dto;


import lombok.Data;

@Data
public class AssignRouteRequest {
    private String routeCode;
    private Long userId;
}
