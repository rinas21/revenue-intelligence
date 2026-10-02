-- V9: provenance and idempotency keys on orders, plus analytics indexes.
--
-- Imports (CSV, Google Sheets) and event consumers must be idempotent: a retry
-- must not create a second sale. external_ref is the caller's stable identifier
-- for the sale (for example a row checksum or an upstream order number), unique
-- per business when present. source records where the order came from.
--
-- The (business_id, order_date) index is the workhorse for the revenue time
-- series, which filters by business and orders by date across a range.

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS source VARCHAR(20),
    ADD COLUMN IF NOT EXISTS external_ref VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uq_orders_business_external_ref
    ON orders (business_id, external_ref)
    WHERE external_ref IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_orders_business_order_date
    ON orders (business_id, order_date);

CREATE INDEX IF NOT EXISTS idx_orders_business_status_date
    ON orders (business_id, status, order_date);
