package com.rinas.revenue.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

/**
 * One rejected row from an import. Carries the row number, a machine-readable
 * code and a human-readable message so the UI can explain the failure.
 */
@Entity
@Table(schema = "app", name = "import_row_errors")
public class ImportRowError {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_job_id", nullable = false)
    private ImportJob importJob;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "raw_row", columnDefinition = "TEXT")
    private String rawRow;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ImportRowError() {
    }

    ImportRowError(ImportJob importJob, int rowNumber, String errorCode, String message, String rawRow) {
        this.id = UUID.randomUUID();
        this.importJob = importJob;
        this.rowNumber = rowNumber;
        this.errorCode = errorCode;
        this.message = message;
        this.rawRow = rawRow;
    }

    public UUID getId() {
        return id;
    }

    public ImportJob getImportJob() {
        return importJob;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getRawRow() {
        return rawRow;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
