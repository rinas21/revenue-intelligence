package com.rinas.revenue.domain;

/**
 * Order lifecycle. Only PAID orders count towards revenue; OPEN is a draft,
 * CANCELLED is void, REFUNDED has been reversed.
 */
public enum OrderStatus {
    OPEN,
    PAID,
    CANCELLED,
    REFUNDED
}
