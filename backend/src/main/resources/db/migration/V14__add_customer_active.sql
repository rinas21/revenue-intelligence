-- V14: soft-deactivation flag for customers.
--
-- Customers are deactivated rather than deleted, for the same reason products
-- are: an order may reference them historically, and deleting the row would
-- either break that order or erase the customer's purchase history. Deactivated
-- customers remain visible on old orders but are hidden from selection lists.

ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX IF NOT EXISTS idx_customers_business_active
    ON customers (business_id, active);
