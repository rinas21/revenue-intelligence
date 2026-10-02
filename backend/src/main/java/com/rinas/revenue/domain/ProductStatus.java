package com.rinas.revenue.domain;

/**
 * Product lifecycle state.
 *
 * <p>Deactivation is preferred over deletion once a product appears on a
 * historical order: deleting it would either break the order or force a cascade
 * that rewrites history. ARCHIVED is the terminal state for products no longer
 * sold but still referenced.
 */
public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    ARCHIVED
}
