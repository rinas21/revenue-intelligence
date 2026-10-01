-- V7: create order_items table
--
-- Items within an order. Every line has a product reference, quantity, and
-- server-calculated line_total = quantity * unit_price - discount_amount.
-- unit_price and discount are server-validated; client-provided values are
-- rejected if they don't match the computation.
--

CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19,4) NOT NULL CHECK (unit_price >= 0),
    discount_amount NUMERIC(19,4) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    line_total NUMERIC(19,4) NOT NULL,

    CONSTRAINT fk_orderitems_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_orderitems_product FOREIGN KEY (product_id) REFERENCES products(id)

    -- line_total = quantity * unit_price - discount_amount, computed server-side
);

CREATE INDEX IF NOT EXISTS idx_orderitems_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_orderitems_product_id ON order_items(product_id);
COMMENT ON COLUMN order_items.line_total IS 'quantity * unit_price - discount_amount, computed server-side';