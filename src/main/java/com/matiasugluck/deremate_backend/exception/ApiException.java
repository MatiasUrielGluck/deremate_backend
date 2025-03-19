package com.matiasugluck.deremate_backend.exception;

import lombok.Getter;

import java.io.Serial;

@Getter
public class ApiException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String code;

    private final String description;

    private final Integer statusCode;

    public ApiException(String code, String description, Integer statusCode) {
        super(description);
        this.code = code;
        this.description = description;
        this.statusCode = statusCode;
    }

    public ApiException(String code, String description, Integer statusCode, Throwable cause) {
        super(description, cause);
        this.code = code;
        this.description = description;
        this.statusCode = statusCode;
    }
}
