package com.rinas.revenue.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A tracked data-ingestion run. Every source (CSV upload, Google Sheet)
 * produces one, and every rejected row produces an {@link ImportRowError}, so
 * "0 imported" is never ambiguous.
 */
@Entity
@Table(schema = "app", name = "import_jobs")
public class ImportJob {

    public enum Source {
        CSV,
        GOOGLE_SHEETS
    }

    public enum Status {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Source source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    @Column(name = "imported_rows", nullable = false)
    private int importedRows;

    @Column(name = "skipped_rows", nullable = false)
    private int skippedRows;

    @Column(name = "failed_rows", nullable = false)
    private int failedRows;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "importJob", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImportRowError> rowErrors = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ImportJob() {
    }

    public ImportJob(Business business, Source source, String fileName) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.source = source;
        this.fileName = fileName;
    }

    public void start() {
        this.status = Status.RUNNING;
        this.startedAt = Instant.now();
    }

    public void recordRow(boolean imported) {
        this.totalRows++;
        if (imported) {
            this.importedRows++;
        }
    }

    public void skipRow() {
        this.totalRows++;
        this.skippedRows++;
    }

    public void addError(int rowNumber, String errorCode, String message, String rawRow) {
        this.totalRows++;
        this.failedRows++;
        this.rowErrors.add(new ImportRowError(this, rowNumber, errorCode, message, rawRow));
    }

    public void complete() {
        this.status = Status.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void fail(String message) {
        this.status = Status.FAILED;
        this.errorMessage = message;
        this.completedAt = Instant.now();
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

    public Source getSource() {
        return source;
    }

    public Status getStatus() {
        return status;
    }

    public String getFileName() {
        return fileName;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getImportedRows() {
        return importedRows;
    }

    public int getSkippedRows() {
        return skippedRows;
    }

    public int getFailedRows() {
        return failedRows;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public List<ImportRowError> getRowErrors() {
        return rowErrors;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
