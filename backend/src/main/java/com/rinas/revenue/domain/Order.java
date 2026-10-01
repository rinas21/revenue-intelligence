package com.rinas.revenue.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "app")
public class Order {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(columnDefinition = "UUID", nullable = false)
    private UUID businessId;

    @Column(columnDefinition = "UUID")
    private UUID customerId;

    @Column(nullable = false)
    private Instant orderDate;

    @Column(nullable = false, length = 50)
    private String status; // OPEN, PAID, CANCELLED, REFUNDED

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // Default constructor for JPA
    public Order() {
        this.id = UUID.randomUUID();
        this.businessId = UUID.randomUUID();
        this.customerId = null;
        this.orderDate = Instant.now();
        this.status = "OPEN";
        this.subtotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Order(UUID businessId, UUID customerId) {
        this.id = UUID.randomUUID();
        this.businessId = businessId;
        this.customerId = customerId;
        this.orderDate = Instant.now();
        this.status = "OPEN";
        this.subtotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public void setBusinessId(UUID businessId) {
        this.businessId = businessId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Instant orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Instant getOrderDateAsInstant() {
        return orderDate;
    }

    public void setOrderDateAsInstant(Instant orderDate) {
        this.orderDate = orderDate;
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