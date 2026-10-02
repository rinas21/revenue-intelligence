package com.rinas.revenue.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published to {@code revenue.order-created} after an order is committed. It is
 * the contract downstream consumers (analytics, Spark streaming) build against,
 * so it carries everything needed to process the sale without querying back.
 *
 * <p>{@code eventId} is unique per event and is the consumer's idempotency key:
 * re-delivery of the same event must not double-count revenue.
 */
public record OrderCreatedEvent(
    UUID eventId,
    UUID orderId,
    UUID businessId,
    UUID customerId,
    Instant orderDate,
    String status,
    BigDecimal subtotal,
    BigDecimal discountAmount,
    BigDecimal totalAmount,
    List<Item> items,
    Instant occurredAt
) {

    public record Item(
        UUID productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal lineTotal
    ) {
    }
}
