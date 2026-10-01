package com.rinas.revenue.common.error;

import java.time.Instant;
import java.util.List;

/**
 * The single error shape returned by every failing API call.
 *
 * @param timestamp when the failure was translated, in UTC
 * @param status    HTTP status code, repeated in the body so a client that only
 *                  reads the payload does not have to correlate two sources
 * @param code      stable machine-readable identifier. Clients branch on this,
 *                  never on {@code message}
 * @param message   short, human-readable summary safe to display
 * @param path      the request path that failed
 * @param errors    per-field detail, present only for validation failures
 */
public record ApiError(
    Instant timestamp,
    int status,
    String code,
    String message,
    String path,
    List<FieldViolation> errors
) {

    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(Instant.now(), status, code, message, path, null);
    }

    public static ApiError of(int status, String code, String message, String path, List<FieldViolation> errors) {
        return new ApiError(Instant.now(), status, code, message, path,
            errors == null || errors.isEmpty() ? null : List.copyOf(errors));
    }

    /**
     * A single rejected field.
     *
     * @param field   dotted path of the offending property, e.g. {@code items[0].quantity}
     * @param message why it was rejected
     */
    public record FieldViolation(String field, String message) {
    }
}
