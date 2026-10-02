-- V12: import jobs and per-row errors.
--
-- An import is a tracked job. Every row is validated, and every rejected row
-- is recorded with its number and a message so the UI can show exactly what
-- failed. Malformed data is never silently dropped: it is either imported or
-- reported.

CREATE TABLE IF NOT EXISTS import_jobs (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    source VARCHAR(20) NOT NULL CHECK (source IN ('CSV', 'GOOGLE_SHEETS')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    file_name VARCHAR(255),
    total_rows INTEGER NOT NULL DEFAULT 0,
    imported_rows INTEGER NOT NULL DEFAULT 0,
    skipped_rows INTEGER NOT NULL DEFAULT 0,
    failed_rows INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_import_jobs_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX IF NOT EXISTS idx_import_jobs_business_created_at
    ON import_jobs (business_id, created_at DESC);

CREATE TABLE IF NOT EXISTS import_row_errors (
    id UUID PRIMARY KEY,
    import_job_id UUID NOT NULL,
    row_number INTEGER NOT NULL,
    error_code VARCHAR(50),
    message TEXT NOT NULL,
    raw_row TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_import_row_errors_job FOREIGN KEY (import_job_id)
        REFERENCES import_jobs(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_import_row_errors_job
    ON import_row_errors (import_job_id, row_number);
