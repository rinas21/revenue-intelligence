package com.rinas.revenue.common.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Verifies the error contract rather than the handler's implementation.
 *
 * <p>The behaviours asserted here are the ones a client depends on: a stable
 * {@code code}, the correct status, field-level detail for validation failures,
 * and — most importantly — that an unexpected exception never reaches the client
 * carrying its own message.
 *
 * <p>Plain Mockito unit tests, not {@code @WebMvcTest}. The handler is a pure
 * function of (exception, request) and there is nothing about the HTTP layer to
 * integrate with, so a full MVC context would add startup cost and buy no
 * coverage. End-to-end verification that the advice is actually wired into the
 * dispatcher happens over HTTP during development.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    private GlobalExceptionHandlerTest() {
        when(request.getRequestURI()).thenReturn("/api/v1/orders");
        when(request.getMethod()).thenReturn("POST");
    }

    private ResponseEntity<ApiError> invoke(Exception ex) {
        return ex instanceof MethodArgumentNotValidException invalid
            ? handler.handleBodyValidation(invalid, request)
            : ex instanceof HttpMessageNotReadableException unreadable
                ? handler.handleUnreadableBody(unreadable, request)
                : handler.handleUnexpected(ex, request);
    }

    @Nested
    @DisplayName("validation failures")
    class Validation {

        @Test
        @DisplayName("reports 400, VALIDATION_ERROR and one entry per rejected field")
        void reportsEachRejectedField() {
            var bindingResult = new BeanPropertyBindingResult(new Object(), "order");
            bindingResult.addError(new FieldError("order", "quantity", "must be greater than zero"));
            bindingResult.addError(new FieldError("order", "customerId", "must not be null"));
            var ex = new MethodArgumentNotValidException(null, bindingResult);

            var response = handler.handleBodyValidation(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
            assertThat(response.getBody().path()).isEqualTo("/api/v1/orders");
            assertThat(response.getBody().errors())
                .extracting(ApiError.FieldViolation::field, ApiError.FieldViolation::message)
                .containsExactly(
                    tuple("quantity", "must be greater than zero"),
                    tuple("customerId", "must not be null"));
        }

        @Test
        @DisplayName("passes through the nested field path so the client can point at the right input")
        void keepsNestedFieldPaths() {
            var bindingResult = new BeanPropertyBindingResult(new Object(), "order");
            // Spring reports a violation on a collection element with the indexed
            // path already in the field name.
            bindingResult.addError(new FieldError("order", "items[0].quantity", "abc", false,
                new String[] { "items[0].quantity" }, null, "must be greater than zero"));
            var ex = new MethodArgumentNotValidException(null, bindingResult);

            var response = handler.handleBodyValidation(ex, request);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).hasSize(1);
            assertThat(response.getBody().errors().getFirst().field()).isEqualTo("items[0].quantity");
        }

        @Test
        @DisplayName("labels a class-level constraint with the target's name")
        void labelsClassLevelConstraints() {
            var bindingResult = new BeanPropertyBindingResult(new Object(), "createOrderRequest");
            bindingResult.addError(new ObjectError("createOrderRequest", "must contain at least one item"));
            var ex = new MethodArgumentNotValidException(null, bindingResult);

            var response = handler.handleBodyValidation(ex, request);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).hasSize(1);
            assertThat(response.getBody().errors().getFirst())
                .extracting(ApiError.FieldViolation::field, ApiError.FieldViolation::message)
                .containsExactly("createOrderRequest", "must contain at least one item");
        }
    }

    @Nested
    @DisplayName("malformed requests")
    class Malformed {

        @Test
        @DisplayName("does not echo the parser message back to the client")
        void doesNotEchoParserMessage() {
            var ex = new HttpMessageNotReadableException(
                "JSON parse error: unexpected character at column 5 near secret-token-abc", null);

            var response = handler.handleUnreadableBody(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("MALFORMED_REQUEST");
            assertThat(response.getBody().message()).doesNotContain("secret-token-abc");
        }
    }

    @Nested
    @DisplayName("unexpected failures")
    class Unexpected {

        @Test
        @DisplayName("returns 500 with a fixed message and leaks no internal detail")
        void leaksNothing() {
            var ex = new IllegalStateException(
                "relation \"app.orders\" does not exist; SQLSTATE 42P01 at /srv/app/OrderRepo.java:88");

            var response = handler.handleUnexpected(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
            assertThat(response.getBody().message())
                .doesNotContain("orders")
                .doesNotContain("42P01")
                .doesNotContain("OrderRepo");
            assertThat(response.getBody().errors()).isNull();
        }
    }

    @Test
    @DisplayName("ApiError omits the errors list when there is nothing to report")
    void omitsEmptyErrorList() {
        var error = ApiError.of(400, "VALIDATION_ERROR", "Request validation failed", "/api/v1/orders",
            List.of());

        assertThat(error.errors()).isNull();
    }
}
