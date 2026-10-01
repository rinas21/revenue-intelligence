package com.rinas.revenue.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI document metadata.
 *
 * <p>Phase 1 has no business endpoints, so this only establishes that the
 * document is generated, is reachable at {@code /v3/api-docs}, and renders in
 * Swagger UI at {@code /swagger-ui.html}. It exists now rather than later so
 * that "the API is documented" is a property of the build from the first
 * endpoint onwards, instead of something to retrofit.
 */
@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI revenueIntelligenceOpenApi() {
        return new OpenAPI().info(new Info()
            .title("Small Business Revenue Intelligence API")
            .version("v1")
            .description("""
                REST API for recording sales and analysing revenue for a small business.

                Every business-owned resource is scoped to a single business, and
                no endpoint may return another business's data. That isolation is
                enforced in the service layer; it is not a property of the URL.

                Errors use a single shape for every failure:

                    {
                      "timestamp": "2026-10-01T09:15:00Z",
                      "status": 400,
                      "code": "VALIDATION_ERROR",
                      "message": "Request validation failed",
                      "path": "/api/v1/orders",
                      "errors": [ { "field": "quantity", "message": "must be greater than zero" } ]
                    }

                Stack traces and internal messages are never returned to clients.
                """));
    }
}
