-- V10: transactional outbox.
--
-- An order is committed to PostgreSQL together with a row in outbox_events in
-- the same transaction. A separate publisher polls PENDING rows and sends them
-- to Kafka, marking them PUBLISHED on acknowledgement. This is what makes
-- "the order exists" and "OrderCreated will be published" a single atomic
-- decision instead of two independent ones that can disagree after a crash.
--
-- See ADR-event-delivery.md. The table is intentionally generic: any aggregate
-- can write here without a schema change.

CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED')),
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ
);

-- The publisher's query: oldest PENDING rows first.
CREATE INDEX IF NOT EXISTS idx_outbox_status_created_at
    ON outbox_events (status, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_aggregate
    ON outbox_events (aggregate_type, aggregate_id);

COMMENT ON TABLE outbox_events IS
    'Transactional outbox: written in the same transaction as the domain change.';
