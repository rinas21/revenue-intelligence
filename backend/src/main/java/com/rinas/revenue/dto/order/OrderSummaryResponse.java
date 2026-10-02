package com.rinas.revenue.dto.order;

import com.rinas.revenue.domain.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * List-row view of an order. It deliberately omits the items collection so a
 * paged list does not trigger a lazy load per row (an N+1). The detail endpoint
 * returns {@link OrderResponse} with the items.
 */
public record OrderSummaryResponse(
    UUID id,
    UUID customerId,
    String customerName,
    Instant orderDate,
    String status,
    BigDecimal totalAmount,
    String source
) {

    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
            order.getId(),
            order.getCustomer() == null ? null : order.getCustomer().getId(),
            order.getCustomer() == null ? null : order.getCustomer().getName(),
            order.getOrderDate(),
            order.getStatus().name(),
            order.getTotalAmount(),
            order.getSource());
    }
}
