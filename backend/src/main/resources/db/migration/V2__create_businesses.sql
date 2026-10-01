-- V2: create businesses table
--
-- Business is the root tenancy unit. Every other business-owned entity
-- references back to a business.
--
-- id: UUID primary key, business-generated or UUID.randomUUID()
-- name: human-readable business name
-- created_at / updated_at: audit timestamps
-- business_id: self-referencing for potential sub-organization hierarchies
-- (NULL = root business, non-NULL = child business under another business)
--
-- Indexes on business_id support application-level tenancy queries.
--
-- @see ADR-tenant-isolation.md

CREATE TABLE IF NOT EXISTS businesses (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    business_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_businesses_business_id ON businesses(business_id);
COMMENT ON COLUMN businesses.business_id IS
    'Self-referencing: NULL = root business, non-NULL = child business. '
    'Enables application-level organizational hierarchies.';