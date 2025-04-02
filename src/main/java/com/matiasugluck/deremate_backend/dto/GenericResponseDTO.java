package com.matiasugluck.deremate_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class GenericResponseDTO<T> {
    private String message;
    private T data; // For successful data (e.g., token)
    @JsonIgnore
    private HttpStatus statusCode; // HTTP Status Code

    public GenericResponseDTO(String message, int statusCode) {
        this.message = message;
        this.statusCode = HttpStatus.valueOf(statusCode);
    }

    public GenericResponseDTO(T data, String message, int statusCode) {
        this.data = data;
        this.message = message;
        this.statusCode = HttpStatus.valueOf(statusCode);
    }
}