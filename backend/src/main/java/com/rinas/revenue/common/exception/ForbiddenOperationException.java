package com.rinas.revenue.common.exception;

import org.springframework.http.HttpStatus;

/** The caller is authenticated but not permitted to perform the operation. */
public class ForbiddenOperationException extends AppException {

    public ForbiddenOperationException(String message) {
        super(HttpStatus.FORBIDDEN, "ACCESS_DENIED", message);
    }
}
