package com.rinas.revenue.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for failures that have a defined meaning to an API client and a
 * stable error code. Anything thrown that is not an {@code AppException} is an
 * unexpected failure and is translated to a generic 500 by
 * {@code GlobalExceptionHandler}.
 */
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    protected AppException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
