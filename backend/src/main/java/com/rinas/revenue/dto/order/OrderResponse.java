package com.rinas.revenue.dto.order;

import com.rinas.revenue.domain.Order;
import com.rinas.revenue.domain.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    UUID customerId,
    String customerName,
    Instant orderDate,
    String status,
    BigDecimal subtotal,
    BigDecimal discountAmount,
    BigDecimal totalAmount,
    String source,
    List<Item> items,
    Instant createdAt,
    Instant updatedAt
) {

    public record Item(
        UUID id,
        UUID productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal lineTotal
    ) {
        public static Item from(OrderItem item) {
            return new Item(
                item.getId(),
                item.getProductId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getDiscountAmount(),
                item.getLineTotal());
        }
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getCustomer() == null ? null : order.getCustomer().getId(),
            order.getCustomer() == null ? null : order.getCustomer().getName(),
            order.getOrderDate(),
            order.getStatus().name(),
            order.getSubtotal(),
            order.getDiscountAmount(),
            order.getTotalAmount(),
            order.getSource(),
            order.getItems().stream().map(Item::from).toList(),
            order.getCreatedAt(),
            order.getUpdatedAt());
    }
}
