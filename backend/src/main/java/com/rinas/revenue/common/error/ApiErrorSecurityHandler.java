package com.rinas.revenue.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Renders security failures in the same {@link ApiError} shape as everything else.
 *
 * <p>This exists because of a gap that is easy to miss. {@code @RestControllerAdvice}
 * never sees a rejection made by the security filter chain: Spring Security's
 * {@code ExceptionTranslationFilter} handles those itself and writes its own
 * response. Left alone, a denied request returns a 403 with an <em>empty body</em>,
 * while a not-found request returns a fully populated {@code ApiError}. A client
 * written against "every error has this shape" would still be broken by the 403.
 *
 * <p>Registered in {@code SecurityConfig}. The entry point (401) is inert in Phase 1
 * because nothing requires authentication yet; it is wired now so that adding
 * authentication in Phase 14 cannot silently introduce a second error format.
 */
@Component
public class ApiErrorSecurityHandler implements AccessDeniedHandler, AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(ApiErrorSecurityHandler.class);

    private final ObjectMapper objectMapper;

    public ApiErrorSecurityHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException ex) throws IOException {
        // A client error: logged at debug, not error, so genuine server faults stay
        // visible in the log.
        log.debug("Access denied for {} {}", request.getMethod(), request.getRequestURI());
        write(request, response, HttpStatus.FORBIDDEN, "ACCESS_DENIED",
            "You do not have access to this resource");
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException ex) throws IOException {
        log.debug("Unauthenticated request to {} {}", request.getMethod(), request.getRequestURI());
        write(request, response, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED",
            "Authentication is required to access this resource");
    }

    private void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
            String code, String message) throws IOException {
        var error = ApiError.of(status.value(), code, message, request.getRequestURI());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
