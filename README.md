# Small Business Revenue Intelligence Platform

A revenue and customer analytics platform for small businesses. A business
records sales, and the system turns that into revenue metrics, product
performance, customer behaviour and automatically generated business insights.

Built as a **modular monolith** in Java, with a data platform around it
(analytics engineering with dbt, orchestration with Airflow, large-scale
processing with Spark) added only where a real problem requires it.

> **Status: Phase 1 — Foundation.**
> Infrastructure, configuration, error handling and API documentation are in
> place. There are no business endpoints yet; those arrive in Phase 2.
> See [Project status](#project-status).

---

## Table of contents

- [Why this project](#why-this-project)
- [Architecture](#architecture)
- [Technology responsibilities](#technology-responsibilities)
- [Technology stack](#technology-stack)
- [Repository layout](#repository-layout)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [API documentation](#api-documentation)
- [Testing](#testing)
- [Project status](#project-status)
- [Documentation](#documentation)

---

## Why this project

Most "revenue dashboard" projects stop at CRUD plus a chart. This one is built
to answer the questions that actually come up in engineering interviews, in
their natural order:

1. **Can you model a domain correctly?** Multi-tenant business data, monetary
   values that are exact rather than approximate, and database constraints that
   protect the data even when application code does not.
2. **Can you design an API a client can rely on?** DTOs that never leak
   persistence types, one error shape for every failure, and no stack traces
   leaving the process.
3. **Do you know when a technology earns its place?** Kafka, Redis, dbt, Airflow
   and Spark are each introduced in the phase where they remove a specific
   problem — and each has an ADR recording what it is explicitly *not* for.
4. **Can you test what matters?** Real PostgreSQL and Kafka through
   Testcontainers rather than mocked persistence, testing behaviour instead of
   chasing coverage numbers.

The through-line: **every technology must have a defensible reason to exist.**

---

## Architecture

```
┌─────────────────────────────┐
│  React + TypeScript + Vite  │   dashboard
└──────────────┬──────────────┘
               │ REST (JSON)
               ▼
┌─────────────────────────────┐
│  Spring Boot 4 / Java 21    │   modular monolith
│  REST · domain · security   │
│  transactions · publishing  │
└───┬──────────┬───────────┬───┘
    │          │           │
    ▼          ▼           ▼
┌────────┐ ┌────────┐ ┌──────────┐
│Postgres│ │  Redis │ │  Kafka   │
│  OLTP  │ │ cache  │ │  events  │
│app +   │ │        │ │          │
│analytics│└────────┘ └────┬─────┘
└────┬────┘                 │
     │                      ▼
     │              event processing
     ▼                      │
 analytics layer            │
     │                      │
     ├── dbt (transform) ───┤
     ├── Spark (large data) ┘
     │        │
     │        └── orchestrated by Airflow
     ▼
 analytical models
     │
     ▼
 automated insights
```

### Data flow, end to end

```
Manual entry / CSV / Google Sheets
              │
              ▼
       normalization + validation
              │
              ▼
    PostgreSQL (the source of truth)
              │
      ┌───────┴────────┐
      ▼                ▼
    Kafka          analytics
      │                │
      ▼                ▼
 event pipeline        dbt
      │                │
      └───────┬────────┘
              ▼
       analytical models
              │
      ┌───────┴────────┐
      ▼                ▼
   dashboard         insights
```

All ingestion sources converge on the same normalized domain model. The
analytics layer does not know or care where a sale came from.

---

## Technology responsibilities

Each technology has one job. The full table, including what each is
**explicitly not** for, is in
[ADR 0003](docs/decisions/0003-technology-responsibilities.md).

| Technology | Responsibility |
|---|---|
| **Java + Spring Boot** | Business application, APIs, domain logic, transactions, security, insight generation |
| **PostgreSQL** | Transactional source of truth; `app` and `analytics` schemas |
| **Kafka** | Asynchronous domain events, decoupling event production from processing |
| **Redis** | Caching expensive analytics; rate limiting |
| **dbt** | Analytics transformation: staging → intermediate → marts, with tests |
| **Airflow** | Orchestration: what runs, when, in what order |
| **Spark** | Large-scale batch and streaming processing |
| **React + TypeScript** | User interface |

Two rules follow from this and are enforced throughout:

- **Kafka never wraps a database transaction.** The order is committed to
  PostgreSQL first; `OrderCreated` is published after.
- **Spark is never on the request path.** The main application does not depend
  on Spark at all.

---

## Technology stack

| Layer | Choice |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.8 (Spring Framework 7, Hibernate 7, Spring Security 7) |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL 17, Flyway 11 |
| Validation | Jakarta Bean Validation (Hibernate Validator) |
| Event streaming | Apache Kafka 4 (KRaft) — configured, unused until Phase 4 |
| Caching | Redis 8 — configured, unused until Phase 9 |
| API docs | springdoc-openapi 3.1 (Swagger UI) |
| Monitoring | Spring Boot Actuator — health only, no metrics scraping |
| Frontend | React 19, TypeScript 6, Vite 8, React Router 7, Axios, Recharts |
| Analytics | dbt — Phase 6 |
| Orchestration | Apache Airflow — Phase 7 |
| Processing | Apache Spark (Java) — Phase 12 |
| Containers | Podman + Podman Compose |
| Testing | JUnit 6, Mockito, AssertJ, Spring Security Test, Testcontainers |

Deliberately **not** used: Kubernetes, Prometheus, Grafana, Elasticsearch,
ClickHouse, MLflow, Feast, service meshes, and any microservice decomposition.

---

## Repository layout

```
revenue-intelligence/
├── backend/                  Spring Boot application
│   ├── .mvn/maven.config     local build settings (see ADR 0004)
│   └── src/
│       ├── main/java/com/rinas/revenue/
│       │   ├── config/       security, OpenAPI, Kafka topics
│       │   └── common/error/ the API error contract
│       ├── main/resources/
│       │   ├── application.yaml
│       │   └── db/migration/ Flyway migrations
│       └── test/
├── frontend/                 React + TypeScript + Vite
│   └── src/
│       ├── api/              the single configured Axios instance
│       ├── components/  pages/  hooks/  types/  layouts/  routes/
│       └── main.tsx
├── infrastructure/
│   └── compose/              Podman Compose stack
├── docs/
│   ├── architecture/
│   ├── database/
│   ├── analytics/
│   └── decisions/            ADRs
├── .env.example
├── .gitignore
└── README.md
```

Directories for phases not yet started (`analytics/dbt`, `data-processing/spark`,
`orchestration/airflow`) are **not** created in advance. They appear with their
implementation.

---

## Getting started

### Prerequisites

- Java 21
- Node.js 20+
- Podman with `podman compose` (see [ADR note](#note-on-podman) below)

### 1. Configure the environment

```bash
cp .env.example .env
```

`.env` holds local development defaults only and is gitignored. No credential
in it is a real credential.

### 2. Start the infrastructure

```bash
podman compose --env-file .env -f infrastructure/compose/compose.yaml up -d
podman compose --env-file .env -f infrastructure/compose/compose.yaml ps
```

`--env-file .env` is explicit because Compose looks for `.env` next to the
compose file, not in the repository root. Every value in the compose file also
carries an inline default, so omitting the flag still works — the variables are
simply ignored.

This starts three services:

| Service   | Port  | Purpose |
|-----------|-------|---------|
| `postgres`| 5432  | Source of truth. Also hosts the `analytics` schema. |
| `redis`   | 6379  | Cache. Persistence is off on purpose — see the note below. |
| `kafka`   | 9092  | Single-node KRaft broker. No ZooKeeper. |

Start a subset with `podman compose --env-file .env -f infrastructure/compose/compose.yaml up -d postgres`.

Stop everything, keeping data:

```bash
podman compose --env-file .env -f infrastructure/compose/compose.yaml down
```

Stop everything and delete the volumes:

```bash
podman compose --env-file .env -f infrastructure/compose/compose.yaml down -v
```

If a host port is already taken, change it in `.env` — for example
`POSTGRES_PORT=5434` together with `DB_PORT=5434`, since the application builds
its JDBC URL from the two.

<details>
<summary>Note on Podman</summary>

`redis` runs with `--save "" --appendonly no`. A cache that must survive a
restart is a cache quietly becoming a database. Losing cached analytics on
restart is the correct failure mode.

`kafka` is a **single-node KRaft cluster with replication factor 1**. It exists
for development, not for production. A real deployment needs at least three
controllers, replication factor 3, and SASL or TLS.

</details>

### 3. Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

Flyway applies the migrations on startup, Hibernate validates the schema against
the (currently empty) entity model, and the application starts on
<http://localhost:8080>.

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

<http://localhost:5173>. The dev server proxies `/api` and `/actuator` to
`http://localhost:8080`, so the browser stays on one origin and no CORS
configuration is needed in development.

---

## Configuration

Every setting is environment-driven, and the defaults match the compose stack, so
the application starts with no `.env` file at all.

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `POSTGRES_DB` | `revenue_intelligence` | Database name |
| `POSTGRES_USER` | `revenue` | Database user |
| `POSTGRES_PASSWORD` | `revenue_local_dev` | Database password — **local only** |
| `DB_POOL_MAX_SIZE` | `10` | HikariCP maximum pool size |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis endpoint |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |
| `KAFKA_CONSUMER_GROUP` | `revenue-intelligence` | Consumer group identity |
| `BACKEND_PORT` | `8080` | HTTP port |
| `BACKEND_URL` | `http://localhost:8080` | Vite dev-server proxy target |
| `FRONTEND_PORT` | `5173` | Vite dev-server port |
| `VITE_API_BASE_URL` | *(unset)* | API base URL; leave unset to use same-origin `/api` |

### Secrets

No password, API key, OAuth secret or connection string is committed. The
defaults in `.env.example` and `application.yaml` are non-secret local values.
Real deployments must supply real values through the environment.

---

## API documentation

| Resource | URL |
|---|---|
| OpenAPI document | <http://localhost:8080/v3/api-docs> |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| Health | <http://localhost:8080/actuator/health> |

### Error contract

Every failure returns the same shape. Clients branch on `code`, never on
`message`.

```json
{
  "timestamp": "2026-10-01T09:15:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/orders",
  "errors": [
    { "field": "quantity", "message": "must be greater than zero" }
  ]
}
```

`errors` is present only for validation failures. Stack traces and internal
messages are never returned to clients — an unexpected exception is logged and
the caller receives a fixed message. This is asserted by a test, because it is
the easiest property in a Spring service to lose by accident.

---

## Testing

```bash
cd backend
./mvnw test
```

Current suite:

| Test | What it protects |
|---|---|
| `GlobalExceptionHandlerTest` | The error contract: stable codes, correct statuses, field-level validation detail, and that internal detail never reaches a client. |
| `RevenueIntelligenceApplicationTests` | The Spring context starts against a real PostgreSQL. |

`RevenueIntelligenceApplicationTests` is a `@SpringBootTest`, so it needs the
infrastructure running:

```bash
podman compose -f infrastructure/compose/compose.yaml up -d postgres
```

Testcontainers-based tests that provision their own PostgreSQL, Kafka and Redis
arrive with the features that need them (Phases 4, 9 and 15). Mocking the
database for integration tests is not an approach this project takes.

Frontend:

```bash
cd frontend
npm run lint
npm run build      # tsc -b, type-checks with `strict: true`
```

---

## Project status

Phases are built in order. Each one is implemented, tested, configured and
documented before the next begins.

| Phase | Scope | Status |
|---|---|---|
| 1 | Foundation: Spring Boot, PostgreSQL, Flyway, security foundation, validation, Actuator, OpenAPI, React, Podman | **Done** |
| 2 | Core domain: Business, User, Product, Customer, Order, OrderItem | Next |
| 3 | Manual sales: React → REST → service → PostgreSQL | Planned |
| 4 | Kafka: `OrderCreated` publication and consumers | Planned |
| 5 | Dashboard: revenue, orders, AOV, daily revenue, top products | Planned |
| 6 | Analytics engineering: dbt staging → intermediate → marts | Planned |
| 7 | Orchestration: Airflow `daily_revenue_pipeline` | Planned |
| 8 | Automated insights: deterministic business rules | Planned |
| 9 | Redis: dashboard query caching, rate limiting | Planned |
| 10 | CSV ingestion | Planned |
| 11 | Google Sheets ingestion | Planned |
| 12 | Spark: batch, then Structured Streaming | Planned |
| 13 | Advanced analytics: cohorts, RFM, growth, anomalies | Planned |
| 14 | Security: authentication, authorization, roles, isolation | Planned |
| 15 | Comprehensive Testcontainers suite | Planned |
| 16 | ML, once the pipeline has enough data | Planned |

### What exists in Phase 1

- Spring Boot 4 / Java 21 application that starts against a real PostgreSQL,
  connects to Kafka and Redis, and reports the health of all three.
- Flyway-owned schema: `app` and `analytics`, created by `V1__create_schemas.sql`.
- Explicit development security. Form login, HTTP Basic and Spring's generated
  password are all switched off, and anything not explicitly permitted is denied.
- Centralized error handling with a single response shape.
- OpenAPI document and Swagger UI.
- A Kafka topic, `revenue.order-created`, provisioned declaratively.
- Podman Compose stack for PostgreSQL, Kafka and Redis.
- Frontend wired to the API through a single Axios instance, `strict: true`.

### What deliberately does not exist yet

No domain entities. No business endpoints. No authentication. No Kafka
producers or consumers. No cache entries. No dbt, Airflow or Spark.

Kafka and Redis are configured but idle, and that is intentional: configuration
is cheap and reversible, but wiring a producer or a cache into a business
transaction before the transaction exists would be guessing. They are configured
now so the connection and health reporting are exercised in the phase that needs
them, rather than in the phase that needs them.

---

## Documentation

| Document | Purpose |
|---|---|
| [ADR 0001 — Modular monolith](docs/decisions/0001-modular-monolith.md) | Why one deployable, and when to revisit |
| [ADR 0002 — PostgreSQL as source of truth](docs/decisions/0002-postgresql-as-source-of-truth.md) | Why one database, and the `app` / `analytics` boundary |
| [ADR 0003 — Technology responsibilities](docs/decisions/0003-technology-responsibilities.md) | What each technology does, and what it must not be used for |
| [ADR 0004 — Build without `--release`](docs/decisions/0004-build-without-release-flag.md) | A local JDK limitation, worked around without changing the Java installation |

New architectural decisions get a new ADR in `docs/decisions/`. Adding a
technology requires one, and the bar is a measured constraint rather than an
interesting article.
