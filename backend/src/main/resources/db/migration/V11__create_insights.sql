-- V11: automated insights.
--
-- Insights are deterministic findings produced by rules over the analytics
-- layer: "revenue fell 23% versus the prior period", "the top product
-- changed". They are stored so the dashboard can list them, and dedupe_key
-- prevents the same finding being inserted on every pipeline run.

CREATE TABLE IF NOT EXISTS insights (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL
        CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL')),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    metric VARCHAR(100) NOT NULL,
    metric_value NUMERIC(19,4),
    baseline_value NUMERIC(19,4),
    change_pct NUMERIC(10,4),
    period_start DATE,
    period_end DATE,
    dedupe_key VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_insights_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX IF NOT EXISTS idx_insights_business_created_at
    ON insights (business_id, created_at DESC);

CREATE UNIQUE INDEX IF NOT EXISTS uq_insights_dedupe
    ON insights (business_id, dedupe_key)
    WHERE dedupe_key IS NOT NULL;
