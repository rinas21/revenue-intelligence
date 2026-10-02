package com.rinas.revenue.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A request to record a sale. Totals are never accepted from the client: the
 * server computes them from the items. {@code unitPrice} is optional; when
 * omitted the product's current price is used. {@code externalRef} is an
 * optional caller-supplied idempotency key.
 */
public record OrderRequest(
    UUID customerId,
    Instant orderDate,
    @DecimalMin(value = "0.0", message = "discount must be zero or greater")
    @Digits(integer = 15, fraction = 4) BigDecimal discountAmount,
    @Size(max = 20) String source,
    @Size(max = 255) String externalRef,
    @NotEmpty(message = "an order must contain at least one item")
    @Valid List<OrderItemRequest> items
) {

    public record OrderItemRequest(
        @NotNull UUID productId,
        @NotNull @jakarta.validation.constraints.Positive(message = "quantity must be greater than zero")
        Integer quantity,
        @DecimalMin(value = "0.0", message = "unit price must be zero or greater")
        @Digits(integer = 15, fraction = 4) BigDecimal unitPrice,
        @DecimalMin(value = "0.0", message = "discount must be zero or greater")
        @Digits(integer = 15, fraction = 4) BigDecimal discountAmount
    ) {
    }
}
