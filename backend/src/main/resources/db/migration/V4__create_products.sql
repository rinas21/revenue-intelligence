-- V4: create products table
--
-- Products belong to a business. Price is exact (numeric(19,4)), never float.
-- status controls visibility: ACTIVE | INACTIVE | ARCHIVED.
-- Deactivation preferred over deletion when historical orders exist.
--

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(19,4) NOT NULL CHECK (price >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    business_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_products_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX IF NOT EXISTS idx_products_business_id ON products(business_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
COMMENT ON COLUMN products.price IS 'Exact monetary value: numeric(19,4), never floating-point.';