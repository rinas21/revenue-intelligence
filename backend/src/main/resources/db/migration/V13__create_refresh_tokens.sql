-- V13: refresh tokens for JWT authentication.
--
-- Access tokens are short-lived and stateless. Refresh tokens are persisted so
-- that logout, role changes and compromise response can actually revoke a
-- session instead of waiting for the access token to expire. Only a hash of the
-- refresh token is stored, never the token itself.

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens (user_id);

-- Seed the first business so a fresh checkout has somewhere to register the
-- first OWNER. This is a fixed, well-known id used only for bootstrap; it is
-- not a credential and grants no access on its own. Registration still
-- requires choosing a username and password.
INSERT INTO businesses (id, name, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'Demo Business', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
