-- V6: create orders table
--
-- An order represents a sale transaction.
-- customer_id is nullable — anonymous/walk-in sales are allowed.
-- status tracks the order lifecycle.
-- All monetary values are server-calculated from order items; client-provided
-- totals are validated on the backend.
--

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    customer_id UUID,
    order_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    subtotal NUMERIC(19,4) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
    total_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_orders_business FOREIGN KEY (business_id) REFERENCES businesses(id),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id)

    -- customer_id is nullable: anonymous sales do not require a customer
);

CREATE INDEX IF NOT EXISTS idx_orders_business_id ON orders(business_id);
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
COMMENT ON COLUMN orders.status IS 'OPEN | PAID | CANCELLED | REFUNDED';
COMMENT ON COLUMN orders.subtotal IS 'Sum of order item line_totals before discount';
COMMENT ON COLUMN orders.discount_amount IS 'Order-level discount';
COMMENT ON COLUMN orders.total_amount IS 'subtotal - discount_amount';