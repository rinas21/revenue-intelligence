# ADR 0002 — PostgreSQL is the source of truth, including for initial analytics

- **Status:** Accepted
- **Date:** 2026-10-01
- **Phase:** 1 — Foundation

## Context

A revenue intelligence product could plausibly justify a separate analytical
store — ClickHouse, Druid, a columnar warehouse. Doing that on day one would
mean running and operating two database technologies before a single business
insight has ever been generated, and before there is any evidence about the
query shapes that need optimising.

At the scale of a small business, all transactional data for a business fits
comfortably in PostgreSQL. Revenue aggregations are `SUM`/`GROUP BY` over
indexed date ranges, which PostgreSQL handles comfortably. The real bottleneck
at this scale is almost always the number of round trips from the dashboard, not
the database's aggregation throughput.

## Decision

One PostgreSQL database, two schemas:

| Schema      | Owner                        | Contents |
|-------------|------------------------------|----------|
| `app`       | Spring Boot, via Flyway      | businesses, users, products, customers, orders, order_items, insights, import jobs |
| `analytics` | dbt                          | analytical models |

Rules that keep the boundary honest:

1. **The application never reads from `analytics`.** dbt models exist to be
   queried by analysts, by the Airflow DAG, and by future ML pipelines — not as
   a shortcut around a missing index on an OLTP table.
2. **The application never writes to `analytics`.** Objects there are owned by
   dbt, which will drop and recreate them on every run.
3. **Hibernate uses `ddl-auto=validate`.** Flyway is the only thing that changes
   the schema. Hibernate creating tables would mean the deployed database could
   diverge from version control with no record of how.

One consequence worth stating explicitly: because both schemas live in one
database, there is no network hop between writing an order and reporting on it.
That is why no CDC or replication technology is introduced — see ADR 0003.

## Consequences

- A single backup, a single restore, a single set of credentials to manage.
- Adding dbt later requires no change to how the application reads its own data.
- The analytics path is not insulated from OLTP load. When that becomes a real
  problem the answer is a separate analytical database reached through
  replication, and this ADR is revisited. It will be driven by measurement, not
  by anticipation.
