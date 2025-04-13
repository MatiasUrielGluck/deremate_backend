package com.matiasugluck.deremate_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponseDTO<T> {
    private String message;
    private T data;

    // Código de estado como entero (200, 404, etc.)
    private int statusCode;

    // Constructor solo con mensaje y status
    public GenericResponseDTO(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    // Constructor con datos + mensaje + status
    public GenericResponseDTO(T data, String message, int statusCode) {
        this.data = data;
        this.message = message;
        this.statusCode = statusCode;
    }
}
