-- V1: create the platform's PostgreSQL schemas.
--
-- Flyway is configured with default-schema=app and schemas=[app, analytics], so
-- it also creates these itself. The statements are therefore idempotent on
-- purpose: the migration records the schema topology in version control, where
-- a reader of the repository can discover it, and it stays correct even if the
-- `spring.flyway.schemas` list is edited later.
--
--   app       transactional domain data owned by the Java application
--             (businesses, users, products, customers, orders, order_items,
--             insights, import jobs). Flyway's history table lives here.
--
--   analytics analytical outputs. Written by dbt, not by the application.
--             dbt owns the objects it creates inside this schema.
--
-- No tables are created in this migration. Domain tables arrive in V2 onward,
-- one migration per concern, and are never edited after being applied.

CREATE SCHEMA IF NOT EXISTS app;

CREATE SCHEMA IF NOT EXISTS analytics;

COMMENT ON SCHEMA app IS
    'Transactional domain data owned by the Spring Boot application.';

COMMENT ON SCHEMA analytics IS
    'Analytical models owned by dbt. Not written by the application.';
