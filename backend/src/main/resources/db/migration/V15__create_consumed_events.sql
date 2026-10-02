-- V15: consumer idempotency ledger.
--
-- Kafka guarantees at-least-once delivery, so a consumer can see the same
-- event twice (rebalance, retry, or an outbox row republished after a crash
-- between send and commit). This table lets a consumer make handling
-- idempotent: insert the event id and only perform side effects when the
-- insert is new. It is the complement to external_ref on orders.

CREATE TABLE IF NOT EXISTS consumed_events (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    consumer VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    consumed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_consumed_event UNIQUE (event_id, consumer)
);

CREATE INDEX IF NOT EXISTS idx_consumed_events_event_id ON consumed_events (event_id);
