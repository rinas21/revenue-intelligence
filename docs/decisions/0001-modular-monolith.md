# ADR 0001 — Modular monolith, not microservices

- **Status:** Accepted
- **Date:** 2026-10-01
- **Phase:** 1 — Foundation

## Context

The platform eventually spans Java, PostgreSQL, Kafka, Redis, dbt, Airflow, Spark
and React. That breadth creates a real risk: the temptation to split the Java
application into services so that "the architecture matches the technology
list".

For a system whose entire domain is *record a sale, then ask questions about it*,
that split has no justification. The operations that must be atomic — writing an
order and its items, publishing `OrderCreated` — all belong to the same
transaction boundary, which is the strongest argument for keeping them in one
process. Splitting them would mean a distributed transaction, or an eventual
consistency window in the middle of the single most important write in the
system.

## Decision

One deployable Spring Boot application. Inside it, features are separated by
package, not by network:

```
com.rinas.revenue
├── config        cross-cutting infrastructure beans
├── common        error contract and shared web plumbing
├── <feature>     one package per bounded context, added as features land
└── ...
```

Kafka, dbt, Airflow and Spark are *not* part of the monolith. They are separate
processes with separate responsibilities, reached over a network boundary, and
that boundary is real rather than a package name.

## Consequences

**Accepted costs**

- The whole application must be deployed to change any part of it. Correctable
  with a longer deployment cycle; not correctable cheaply.
- A resource leak in one feature can affect the others. Mitigated by ordinary
  JVM hygiene — bounded pools, timeouts, and Actuator health checks.
- Scaling is uniform. A dashboard query that is slow slows down order creation.
  This is the strongest argument for revisiting the decision, and the reason
  Redis caching (Phase 9) is likely to arrive before any service split.

**Gains**

- Order creation is a single database transaction with no coordination cost.
- A developer runs, tests and debugs the entire application as one process.
- Refactoring across feature boundaries is a compile-time-checked rename, not a
  coordinated multi-repo release.

## When to revisit

Revisit only when a measured constraint forces it: independent scaling
requirements on the analytics path, or a deployment cadence that genuinely
cannot be met by a single artifact. Splitting because a technology was learned
is not a trigger.
