# ADR 0003 — The role of each technology, and what it is explicitly not for

- **Status:** Accepted
- **Date:** 2026-10-01
- **Phase:** 1 — Foundation

## Context

The project spans Java, PostgreSQL, Kafka, Redis, dbt, Airflow, Spark and React.
A portfolio of this shape is usually assembled by adding a technology and
finding a use for it afterwards. The result is a system where every component
exists, none is load-bearing, and an interviewer is left asking the one question
that matters: *why this, and what would break if you removed it?*

This ADR answers that question for each component in advance, including for the
ones not yet built. It is also the reason the *not* column matters as much as the
*is* column.

## Decision

| Technology | Responsible for | Explicitly **not** for |
|---|---|---|
| **Java + Spring Boot** | Business application, REST APIs, domain logic, authentication and authorization, transactional operations, manual sales, product/customer management, event publication, dashboard API, insight generation | Large-scale batch processing; holding data for analytics |
| **PostgreSQL** | Transactional source of truth; `app` and `analytics` schemas; constraints as a protection layer | Being the only query engine for arbitrarily large history |
| **Kafka** | Asynchronous domain events, decoupling event *production* from event *processing* | Replacing the database transaction that writes an order; work that must happen before the API responds |
| **Redis** | Caching expensive dashboard and analytics results; rate limiting | Being a source of truth; storing anything that must not be evictable |
| **dbt** | Analytics transformation: staging → intermediate → marts, with tests and documentation | Transactional writes; application logic; orchestration |
| **Airflow** | Orchestration: what runs, when, and in what order | Synchronous request handling; application business logic |
| **Spark** | Large-scale batch and streaming processing over historical sales data | Anything on the REST request path; the core domain |
| **React + TypeScript** | User interface | Holding business logic; the source of truth for any number |

## The two rules this produces

**1. Kafka does not wrap database transactions.**
An order and its items are written in one PostgreSQL transaction. Only after
that transaction commits is `OrderCreated` published. Publishing first would mean
emitting an event for an order that might never exist. If the gap between commit
and publish ever becomes a real reliability problem, the fix is a transactional
outbox table written inside the same transaction — a deliberate, documented
design, not an assumption that "Kafka transactions solve it".

**2. Spark is never a request-path dependency.**
The main Spring Boot application does not depend on Spark. A dashboard request
does not run a Spark job. If analytics become too large for PostgreSQL, Spark
reads the historical data and writes results; the online path keeps serving from
the database or the cache.

## Consequences

- Each technology earns its place by removing a specific problem, and the phase
  in which it is introduced is tied to the problem appearing.
- Adding a technology requires a new ADR. The bar is a measured constraint, not
  an interesting article.
- Kafka and Redis are configured in Phase 1 but have no producers or cache
  entries yet. Configuration is cheap and reversible; the *use* is what must
  wait for a reason. Neither participates in any business transaction.
