package com.matiasugluck.deremate_backend.config;

import com.matiasugluck.deremate_backend.dto.GenericResponseDTO;
import com.matiasugluck.deremate_backend.exception.ApiError;
import com.matiasugluck.deremate_backend.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    protected ResponseEntity<ApiError> handleApiException(ApiException e) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.value() > e.getStatusCode()) {
            LOGGER.warn("Api warn with status code: {}", e.getStatusCode());
        } else {
            LOGGER.error("Api error with status code: {}", e.getMessage());
        }

        ApiError apiError = new ApiError(e.getCode(), e.getDescription(), e.getStatusCode());
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiError> handleException(Exception e) {
        LOGGER.error("Internal error with status code 500: {}", e.getMessage());

        ApiError apiError = new ApiError("internal_error", "Internal error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Establece el código de estado HTTP directamente
    public ResponseEntity<GenericResponseDTO<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult().getAllErrors().stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error)
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (existingMessage, newMessage) -> existingMessage + "; " + newMessage
                ));

        GenericResponseDTO<Map<String, String>> errorResponse = new GenericResponseDTO<>(
                "Validation Failed",
                errors,
                HttpStatus.BAD_REQUEST.value()
        );


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
