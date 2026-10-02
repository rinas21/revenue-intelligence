package com.rinas.revenue.dto.product;

import com.rinas.revenue.domain.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank @Size(max = 255) String name,
    @Size(max = 5000) String description,
    @NotNull @DecimalMin(value = "0.0", message = "price must be zero or greater")
    @Digits(integer = 15, fraction = 4, message = "price supports at most 4 decimal places") BigDecimal price,
    ProductStatus status
) {
}
