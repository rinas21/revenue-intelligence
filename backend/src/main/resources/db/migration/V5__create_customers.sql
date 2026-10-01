-- V5: create customers table
--
-- Customers belong to a business. Email is optional — anonymous/walk-in sales
-- are permitted (customer_id nullable on orders).
-- phone is optional.
--

CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50),
    business_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_customers_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX IF NOT EXISTS idx_customers_business_id ON customers(business_id);
COMMENT ON COLUMN customers.email IS 'Optional: null = anonymous / walk-in customer';