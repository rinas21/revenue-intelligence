package com.rinas.revenue.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(schema = "app")
public class Product {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, INACTIVE, ARCHIVED

    @Column(columnDefinition = "UUID")
    private UUID businessId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // Default constructor for JPA
    public Product() {
        this.id = UUID.randomUUID();
        this.price = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.status = "ACTIVE";
    }

    public Product(String name, BigDecimal price, UUID businessId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.price = price;
        this.businessId = businessId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.status = "ACTIVE";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public void setBusinessId(UUID businessId) {
        this.businessId = businessId;
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