package com.rinas.revenue.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Root tenancy unit. Every business-owned row carries this business's id in its
 * {@code business_id} column, and the application scopes all reads and writes by
 * it. See {@code ADR-tenant-isolation.md}.
 *
 * <p>{@code parentBusinessId} is an optional self-reference for a future
 * organization hierarchy; a NULL value is a root business. It is not used for
 * access control today.
 */
@Entity
@Table(schema = "app", name = "businesses")
public class Business {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "business_id", columnDefinition = "UUID")
    private UUID parentBusinessId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Business() {
    }

    public Business(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getParentBusinessId() {
        return parentBusinessId;
    }

    public void setParentBusinessId(UUID parentBusinessId) {
        this.parentBusinessId = parentBusinessId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
