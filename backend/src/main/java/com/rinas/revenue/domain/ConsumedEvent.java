package com.rinas.revenue.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/** A record that a given consumer has already handled a given event id. */
@Entity
@Table(schema = "app", name = "consumed_events")
public class ConsumedEvent {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "event_id", nullable = false, columnDefinition = "UUID")
    private UUID eventId;

    @Column(nullable = false, length = 100)
    private String consumer;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @CreationTimestamp
    @Column(name = "consumed_at", nullable = false, updatable = false)
    private Instant consumedAt;

    protected ConsumedEvent() {
    }

    public ConsumedEvent(UUID eventId, String consumer, String eventType) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.consumer = consumer;
        this.eventType = eventType;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getConsumer() {
        return consumer;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }
}
