package com.matiasugluck.deremate_backend.exception;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponseDTO<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(
                new GenericResponseDTO<>("Error de validación", errors, HttpStatus.BAD_REQUEST.value())
        );
    }

    @ExceptionHandler(CustomNotFoundException.class)
    public ResponseEntity<GenericResponseDTO<Object>> handleCustomNotFound(CustomNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new GenericResponseDTO<>(ex.getMessage(), null, HttpStatus.NOT_FOUND.value())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponseDTO<Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new GenericResponseDTO<>("Ocurrió un error inesperado", null, HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
    }
}
