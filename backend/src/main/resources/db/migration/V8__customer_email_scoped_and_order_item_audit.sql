-- V8: multi-tenant correctness for customers, and audit columns on order_items.
--
-- V5 declared customers.email globally UNIQUE. That is wrong for a multi-tenant
-- system: two different businesses may legitimately have the same customer
-- email, and a global constraint lets one business discover whether another
-- business already holds an address. Email uniqueness must be scoped to the
-- owning business.
--
-- V7 created order_items without created_at / updated_at, which every other
-- domain table has. They are added here rather than by editing V7, because a
-- migration that has been applied is never rewritten.

ALTER TABLE customers DROP CONSTRAINT IF EXISTS customers_email_key;

-- Case-insensitive uniqueness within a business, ignoring anonymous rows. A
-- partial unique index is used because a plain UNIQUE(business_id, email) would
-- allow many NULL emails on PostgreSQL, which is what we want, but would treat
-- 'A@x.com' and 'a@x.com' as different customers.
CREATE UNIQUE INDEX IF NOT EXISTS uq_customers_business_email
    ON customers (business_id, LOWER(email))
    WHERE email IS NOT NULL;

ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
