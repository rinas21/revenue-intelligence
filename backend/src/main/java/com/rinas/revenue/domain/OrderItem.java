package com.rinas.revenue.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(schema = "app")
public class OrderItem {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(columnDefinition = "UUID", nullable = false)
    private UUID orderId;

    @Column(columnDefinition = "UUID", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // Default constructor for JPA
    public OrderItem() {
        this.id = UUID.randomUUID();
        this.quantity = 1;
        this.unitPrice = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public OrderItem(UUID orderId, UUID productId) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = 1;
        this.unitPrice = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}