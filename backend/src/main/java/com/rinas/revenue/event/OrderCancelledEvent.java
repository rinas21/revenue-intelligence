package com.rinas.revenue.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Published to {@code revenue.order-cancelled} when an order is voided. */
public record OrderCancelledEvent(
    UUID eventId,
    UUID orderId,
    UUID businessId,
    Instant cancelledAt,
    String reason
) {
}
