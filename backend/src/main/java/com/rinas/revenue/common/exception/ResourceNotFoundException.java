package com.rinas.revenue.common.exception;

import org.springframework.http.HttpStatus;

/**
 * A resource the caller is allowed to know about does not exist. When the
 * resource belongs to another business, callers must receive this same 404
 * rather than a 403: telling them the row exists but is forbidden leaks the
 * existence of another tenant's data.
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("%s %s was not found".formatted(resource, id));
    }
}
