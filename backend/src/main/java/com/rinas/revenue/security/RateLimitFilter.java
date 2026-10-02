package com.rinas.revenue.security;

import com.rinas.revenue.common.error.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Fixed-window rate limiter backed by Redis. Limiting is keyed by the
 * authenticated business where available and by client address otherwise, so
 * one tenant cannot exhaust another's budget and unauthenticated login attempts
 * are still throttled.
 *
 * <p>Like the cache, this fails open: if Redis is down the request is allowed.
 * Availability of the business function is more important than the limiter, and
 * the alternative (a 500 because a counter is unavailable) is worse than a
 * temporarily unthrottled API.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final String PREFIX = "ri:ratelimit:";

    private final StringRedisTemplate redis;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(StringRedisTemplate redis, RateLimitProperties properties, ObjectMapper objectMapper) {
        this.redis = redis;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        long windowSeconds = Math.max(properties.getWindow().toSeconds(), 1);
        long windowBucket = Instant.now().getEpochSecond() / windowSeconds;
        String key = PREFIX + windowBucket + ":" + identity(request);

        try {
            Long count = redis.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redis.expire(key, Duration.ofSeconds(windowSeconds));
            }
            if (count != null && count > properties.getCapacity()) {
                writeTooManyRequests(request, response);
                return;
            }
        } catch (RuntimeException ex) {
            log.debug("Rate limiter unavailable, allowing request: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String identity(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUserPrincipal principal) {
            return "biz:" + principal.getBusinessId();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var error = ApiError.of(HttpStatus.TOO_MANY_REQUESTS.value(), "RATE_LIMIT_EXCEEDED",
            "Too many requests; please slow down", request.getRequestURI());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
