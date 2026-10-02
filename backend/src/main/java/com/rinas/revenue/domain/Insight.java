package com.rinas.revenue.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * A deterministic finding produced by an insight rule. Every insight states
 * what changed, the period, the observed value and the baseline it is compared
 * against, so it can be justified rather than asserted.
 */
@Entity
@Table(schema = "app", name = "insights")
public class Insight {

    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 50)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String metric;

    @Column(name = "metric_value", precision = 19, scale = 4)
    private BigDecimal metricValue;

    @Column(name = "baseline_value", precision = 19, scale = 4)
    private BigDecimal baselineValue;

    @Column(name = "change_pct", precision = 10, scale = 4)
    private BigDecimal changePct;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "dedupe_key", length = 255)
    private String dedupeKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Insight() {
    }

    public Insight(Business business, String type, Severity severity, String title, String description,
            String metric, BigDecimal metricValue, BigDecimal baselineValue, BigDecimal changePct,
            LocalDate periodStart, LocalDate periodEnd, String dedupeKey) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.metric = metric;
        this.metricValue = metricValue;
        this.baselineValue = baselineValue;
        this.changePct = changePct;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.dedupeKey = dedupeKey;
    }

    public UUID getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public UUID getBusinessId() {
        return business == null ? null : business.getId();
    }

    public String getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getMetric() {
        return metric;
    }

    public BigDecimal getMetricValue() {
        return metricValue;
    }

    public BigDecimal getBaselineValue() {
        return baselineValue;
    }

    public BigDecimal getChangePct() {
        return changePct;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public String getDedupeKey() {
        return dedupeKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
