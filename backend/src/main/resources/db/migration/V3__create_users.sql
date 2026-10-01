-- V3: create users table
--
-- Users belong to a business. Roles: OWNER, ADMIN, STAFF.
-- Passwords are never stored plaintext — see SecurityConfig.
-- business_id ties the user to their owning business.
--
-- @see ADR-tenant-isolation.md

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'STAFF')),
    business_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_users_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX IF NOT EXISTS idx_users_business_id ON users(business_id);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
COMMENT ON COLUMN users.role IS 'OWNER | ADMIN | STAFF';