package com.rinas.revenue.config;

import com.rinas.revenue.common.error.ApiErrorSecurityHandler;
import com.rinas.revenue.security.JwtAuthenticationFilter;
import com.rinas.revenue.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration.
 *
 * <p>The API is stateless and authenticated with short-lived bearer tokens.
 * Because there is no session cookie, CSRF protection is not meaningful for the
 * API and is disabled; a token must be presented explicitly on every request.
 *
 * <p>Authorization is deliberately coarse here: authentication endpoints and
 * operational endpoints are public, and everything else requires a valid token.
 * It is intentionally <em>not</em> expressed as per-endpoint rules, because the
 * real isolation boundary is the {@code business_id} carried by the token and
 * enforced in the service layer. A rule that says "this path needs ADMIN" does
 * not stop an ADMIN of Business A reading Business B; the business scope does.
 *
 * <p>Rejections produced by the filter chain never reach
 * {@code GlobalExceptionHandler}, so {@link ApiErrorSecurityHandler} renders
 * them in the same {@code ApiError} shape as every other failure.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/auth/**",
        "/actuator/health",
        "/actuator/health/**",
        "/actuator/info",
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/swagger-ui.html",
        "/swagger-ui/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ApiErrorSecurityHandler securityErrorHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter, RateLimitFilter rateLimitFilter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler(securityErrorHandler)
                .authenticationEntryPoint(securityErrorHandler))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class)
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // Cost 10 is the Spring default and remains an acceptable balance for a
        // local-first application; it is stored in the hash so it can be raised
        // later without invalidating existing passwords.
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
