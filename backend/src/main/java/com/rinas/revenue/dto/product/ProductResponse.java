package com.rinas.revenue.dto.product;

import com.rinas.revenue.domain.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String name,
    String description,
    BigDecimal price,
    String status,
    Instant createdAt,
    Instant updatedAt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStatus().name(),
            product.getCreatedAt(),
            product.getUpdatedAt());
    }
}
