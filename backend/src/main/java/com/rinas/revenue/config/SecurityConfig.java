package com.rinas.revenue.config;

import com.rinas.revenue.common.error.ApiErrorSecurityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Explicit development security configuration.
 *
 * <p>The reasoning here matters, because the default is the problem.
 * {@code spring-boot-starter-security} is on the classpath, and without a
 * {@link org.springframework.security.web.SecurityFilterChain} bean Spring
 * Security would install its own default chain: form login plus HTTP Basic
 * backed by a randomly generated password printed to the log. That is the worst
 * of both outcomes — every API call returns 401 with a credential nobody chose,
 * and the security posture of the application is decided by a default rather
 * than by a decision.
 *
 * <p>So Phase 1 states the posture out loud instead:
 *
 * <ul>
 *   <li>Form login, HTTP Basic and logout are switched <em>off</em> explicitly.
 *       There is no user store yet, so there is no way to authenticate, and a
 *       generated credential would only hide that.</li>
 *   <li>{@code UserDetailsServiceAutoConfiguration} is excluded in
 *       {@code application.yaml}, which is what actually stops the generated
 *       password from being created.</li>
 *   <li>Sessions are stateless and CSRF protection is disabled. This is sound
 *       only because the API is stateless and unauthenticated: there is no
 *       session cookie for a cross-site request to ride on. Phase 14 revisits
 *       this — if authentication ends up using a cookie rather than a bearer
 *       token, CSRF protection must be turned back on.</li>
 *   <li>{@code /api/**} is permitted so the frontend can be developed against a
 *       real backend. Everything not explicitly listed is denied, so a new
 *       endpoint is unreachable until it is deliberately allowed.</li>
 * </ul>
 *
 * <p>Business isolation is not implemented here. It belongs with the domain
 * model in Phase 2 and is completed in Phase 14; this configuration is only the
 * foundation that makes the development loop possible.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    /** Endpoints that must work before authentication exists. */
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/**",
        "/actuator/health",
        "/actuator/health/**",
        "/actuator/info",
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/swagger-ui.html",
        "/swagger-ui/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ApiErrorSecurityHandler securityErrorHandler)
            throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            // Rejections made here never reach GlobalExceptionHandler, so they get
            // their own writer. Without this a denied request would answer with an
            // empty body while every other failure returns ApiError.
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler(securityErrorHandler)
                .authenticationEntryPoint(securityErrorHandler))
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().denyAll())
            .build();
    }
}
