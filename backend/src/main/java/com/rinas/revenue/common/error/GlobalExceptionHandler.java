package com.rinas.revenue.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates every exception that escapes a controller into the {@link ApiError}
 * shape.
 *
 * <p>Two rules govern this class.
 *
 * <p><strong>One shape, always.</strong> A client parsing an error should not
 * have to know whether the failure came from validation, from a missing route, or
 * from an unhandled exception. That is why this is a single advice rather than
 * per-controller handling.
 *
 * <p><strong>Nothing internal leaks.</strong> The 500 handler logs the original
 * exception and returns a fixed message. Returning {@code ex.getMessage()} for an
 * unexpected exception is the most common way a Spring service leaks SQL text,
 * class names and file paths to the internet, and it is why the last test in
 * {@code GlobalExceptionHandlerTest} exists.
 *
 * <p>Client errors (4xx) are not logged. They are the caller's problem and
 * logging them at WARN on every bad request buries the server errors that
 * actually need attention.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Returned verbatim for unexpected failures. Never an exception message. */
    private static final String INTERNAL_ERROR_MESSAGE =
        "An unexpected error occurred. The failure has been logged.";

    // --- 400 Bad Request ----------------------------------------------------

    /**
     * Bean Validation failures on a {@code @RequestBody}. The common case, and the
     * only one that reports individual fields.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = ex.getBindingResult().getAllErrors().stream()
            .map(GlobalExceptionHandler::toFieldViolation)
            .toList();

        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed",
            request, violations);
    }

    /**
     * Bean Validation failures on method parameters — {@code @PathVariable},
     * {@code @RequestParam} and {@code @RequestHeader} constraints. Spring raises
     * this instead of {@code ConstraintViolationException} when MVC performs the
     * validation, so both have to be handled to get complete field reporting.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiError> handleMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = ex.getParameterValidationResults().stream()
            .flatMap(result -> result.getResolvableErrors().stream()
                .map(error -> new ApiError.FieldViolation(
                    result.getMethodParameter().getParameterName(),
                    error.getDefaultMessage())))
            .toList();

        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed",
            request, violations);
    }

    /** Bean Validation failures raised outside the MVC request lifecycle. */
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = ex.getConstraintViolations().stream()
            .map(GlobalExceptionHandler::toFieldViolation)
            .toList();

        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed",
            request, violations);
    }

    /**
     * Unparseable or absent JSON. The parser's own message can quote the
     * submitted payload, so it is deliberately not echoed back.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
            "Request body is missing or is not valid JSON", request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
            "Parameter '%s' has an invalid value".formatted(ex.getName()), request, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
            "Required parameter '%s' is missing".formatted(ex.getParameterName()), request, List.of());
    }

    // --- 404 Not Found ------------------------------------------------------

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
            "No endpoint matches %s %s".formatted(request.getMethod(), request.getRequestURI()), request, List.of());
    }

    // --- 403 Forbidden ------------------------------------------------------

    /**
     * Authorization denial raised inside the dispatcher. Reached once method
     * security exists in Phase 14; handled now so a denial cannot surface as a
     * 500 with an internal message.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<ApiError> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
            "You do not have access to this resource", request, List.of());
    }

    // --- 405 / 415 ----------------------------------------------------------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
            "Method %s is not supported for this endpoint".formatted(request.getMethod()), request, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
            "Content type %s is not supported; use application/json".formatted(ex.getContentType()),
            request, List.of());
    }

    // --- 4xx domain failures ------------------------------------------------

    /**
     * Every deliberate domain failure carries its own HTTP status and stable
     * code, so it is translated here in one place rather than by a handler per
     * subclass. This is where later phases plug in without introducing a second
     * error format.
     */
    @ExceptionHandler(com.rinas.revenue.common.exception.AppException.class)
    ResponseEntity<ApiError> handleAppException(com.rinas.revenue.common.exception.AppException ex,
            HttpServletRequest request) {
        return response(ex.getStatus(), ex.getCode(), ex.getMessage(), request, List.of());
    }

    // --- 500 ----------------------------------------------------------------

    /**
     * Last resort. The stack trace goes to the log; the client gets a fixed
     * message. Declaring this handler also stops Spring's default error page
     * from rendering its own body, which would break the single-shape rule.
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", INTERNAL_ERROR_MESSAGE, request, List.of());
    }

    // --- helpers ------------------------------------------------------------

    /**
     * {@code FieldError.getField()} already carries the full path for nested and
     * collection elements — {@code items[0].quantity}, not {@code quantity} — so
     * it is passed through untouched. A non-field {@code ObjectError} comes from a
     * class-level constraint such as {@code @ValidOrder}, where there is no field
     * to report and the target's name is the most useful label.
     */
    private static ApiError.FieldViolation toFieldViolation(ObjectError error) {
        String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
        return new ApiError.FieldViolation(field, error.getDefaultMessage());
    }

    private static ApiError.FieldViolation toFieldViolation(ConstraintViolation<?> violation) {
        return new ApiError.FieldViolation(violation.getPropertyPath().toString(), violation.getMessage());
    }

    private static ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
            HttpServletRequest request, List<ApiError.FieldViolation> violations) {
        return ResponseEntity.status(status)
            .body(ApiError.of(status.value(), code, message, request.getRequestURI(), violations));
    }
}
