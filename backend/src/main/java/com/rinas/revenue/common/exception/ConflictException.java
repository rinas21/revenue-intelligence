package com.rinas.revenue.common.exception;

import org.springframework.http.HttpStatus;

/** The request conflicts with current state, e.g. a duplicate unique value. */
public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "CONFLICT", message);
    }

    public static ConflictException duplicate(String what) {
        return new ConflictException(what + " already exists");
    }
}
