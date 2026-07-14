---
title: "Database Selection for Generated Projects"
status: final
created: 2026-06-06
updated: 2026-06-06
project: operaton-starter
---

# PRD: Database Selection for Generated Projects

## Overview

operaton-starter currently generates PROCESS_APPLICATION projects with H2 in-memory hardcoded as the datasource. Developers who want a real database must manually reconfigure the generated project. This PRD adds **database selection** as a first-class build option — alongside the existing Docker Compose toggle — so that generated projects arrive pre-configured for the chosen database.

**Scope:** PROCESS_APPLICATION project type only.

---

## Open Questions

| # | Question | Owner | Condition to resolve |
|---|---|---|---|
| OQ-1 | Should Gradle builds use the Spring Boot BOM for JDBC driver version management, or pin versions explicitly in the template? | Engineering | Before implementation |
| OQ-2 | Oracle `gvenzl/oracle-free` first-start takes ~2 min. What `start_period` value should the generated Docker Compose healthcheck use? | Engineering | Before template implementation |
| OQ-3 | DB2 `icr.io/db2_community/db2`: what are the mandatory environment variables (database name, username, password) required by the image? These must be reflected in the generated `docker-compose.yml` and `application-docker.properties`. | Engineering | **Blocker** — must be resolved before DB2 template is authored |

---

## Problem Statement

Developers bootstrapping a real Operaton project need a datasource matching their target environment (PostgreSQL, MySQL, etc.). The current generated project forces a manual reconfiguration step:

1. Swap the H2 dependency for the correct JDBC driver
2. Rewrite `application.properties` with the correct JDBC URL, driver class, and credentials
3. If using Docker Compose: add a database service manually
4. Write README instructions for themselves or their team

This friction erodes the "immediately runnable" promise of operaton-starter, especially for developers moving past evaluation into real project setup.

---

## Goals

- Generated PROCESS_APPLICATION projects arrive pre-configured for the selected database with no manual datasource edits required
- H2 remains the default — zero-friction path for evaluation unchanged
- Docker Compose enabled + non-H2 selected → generated `docker-compose.yml` includes a ready-to-use database service
- Docker Compose not enabled → generated README explains how to set up an external database
- All databases Operaton supports are available as options: H2, PostgreSQL, MySQL, MariaDB, Oracle, MS SQL Server, DB2

---

## Non-Goals

- Database selection for PROCESS_ARCHIVE or DMN project types
- Schema migration tooling (Flyway/Liquibase) in generated projects
- Oracle/DB2 Docker Compose images match Operaton's test suite; registry authentication is the user's responsibility, documented in the generated README

---

## Success Metrics

- Adoption: ≥30% of generated PROCESS_APPLICATION projects use a non-H2 database within 90 days of release
- Quality: zero bug reports against generated projects that fail to compile or start due to datasource misconfiguration
- Coverage: CI matrix green across all (buildSystem × databaseOption × dockerCompose) combinations at merge

---

## User Stories

**US-1 — PostgreSQL + Docker Compose** → FR-1, FR-2, FR-3, FR-4, FR-5
*I select PostgreSQL + Docker Compose. The generated project includes a working `docker-compose.yml` with a PostgreSQL service and `application-docker.properties`. `docker compose up` starts the application connected to PostgreSQL.*

**US-2 — PostgreSQL, no Docker Compose** → FR-1, FR-2, FR-3, FR-5
*I select PostgreSQL without Docker Compose. The generated project includes `application-local.properties` with `localhost`-default connection properties and README instructions. `mvn spring-boot:run` connects to my local database.*

**US-3 — H2 default unchanged** → FR-1, FR-3
*I generate a project with default settings. `mvn spring-boot:run` works immediately with no database setup.*

**US-4 — Oracle + Docker Compose** → FR-1, FR-2, FR-3, FR-4, FR-5
*I select Oracle + Docker Compose. The generated project includes a `docker-compose.yml` using `gvenzl/oracle-free`, `application-docker.properties` with the Oracle JDBC URL, and a README section explaining Oracle container image setup.*

---

## Functional Requirements

### FR-1: DatabaseOption enum on ProjectConfig

Add `DatabaseOption` to the domain model with values: `H2`, `POSTGRESQL`, `MYSQL`, `MARIADB`, `MSSQL`, `ORACLE`, `DB2`. Default: `H2`.

`ProjectConfig` gains a `databaseOption` field. All generation channels (REST API, CLI, MCP) accept and propagate this value.

### FR-2: JDBC dependency in generated build file

The generated `pom.xml` (or Gradle equivalent) includes the correct JDBC driver dependency for the selected database, all at `runtime` scope. Versions are managed via Spring Boot's BOM where available; pinned explicitly otherwise. Renovate/Dependabot keeps them current. (See OQ-1 for Gradle BOM resolution.)

| Database | Maven coordinates |
|---|---|
| H2 | `com.h2database:h2` (already present) |
| PostgreSQL | `org.postgresql:postgresql` |
| MySQL | `com.mysql:mysql-connector-j` |
| MariaDB | `org.mariadb.jdbc:mariadb-java-client` |
| MS SQL Server | `com.microsoft.sqlserver:mssql-jdbc` |
| Oracle | `com.oracle.database.jdbc:ojdbc11` |
| DB2 | `com.ibm.db2:jcc` |

### FR-3: Three-profile datasource configuration

| Selected database | Profile on `mvn spring-boot:run` | Profile in Docker Compose |
|---|---|---|
| H2 | *(none — H2 is the `application.properties` default)* | *(none)* |
| Non-H2 | `local` (default in `application.properties`) | `docker` (set by Docker Compose env) |

For **H2**, only `application.properties` is generated — no profile files. `mvn spring-boot:run` starts against H2 with no arguments.

For **non-H2**, `application.properties` sets `spring.profiles.active=local` so that `mvn spring-boot:run` connects to the developer's local database via `application-local.properties`. No flags are needed to connect to a real database on first run.

**`application-local.properties`** — targets a locally installed database instance (default profile outside Docker):
```
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:operaton}
spring.datasource.username=${DB_USER:operaton}
spring.datasource.password=${DB_PASSWORD:operaton}
spring.datasource.driver-class-name=org.postgresql.Driver
operaton.bpm.database.schema-update=true
```
(example for PostgreSQL; equivalent generated for each database)

**`application-docker.properties`** — activated by Docker Compose via `SPRING_PROFILES_ACTIVE=docker`:
```
spring.datasource.url=jdbc:postgresql://db:5432/${DB_NAME:operaton}
spring.datasource.username=${DB_USER:operaton}
spring.datasource.password=${DB_PASSWORD:operaton}
spring.datasource.driver-class-name=org.postgresql.Driver
operaton.bpm.database.schema-update=true
```

All values use `${KEY:default}` notation — environment-variable-overridable, twelve-factor compliant, no hardcoded passwords.

**JDBC URL forms per database** (exact syntax required by each driver):

| Database | JDBC URL pattern | Notes |
|---|---|---|
| PostgreSQL | `jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:operaton}` | |
| MySQL | `jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:operaton}?sessionVariables=transaction_isolation=READ-COMMITTED&sendFractionalSeconds=false` | No quotes around value — required by Connector/J 8+; `sendFractionalSeconds=false` matches Operaton testcontainer config |
| MariaDB | `jdbc:mariadb://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:operaton}?sessionVariables=transaction_isolation=READ-COMMITTED` | MariaDB connector accepts this form |
| MS SQL Server | `jdbc:sqlserver://${DB_HOST:localhost}:${DB_PORT:1433};databaseName=operaton_test;encrypt=false` | `encrypt=false` for local dev; `operaton_test` matches Operaton testcontainer convention |
| Oracle | `jdbc:oracle:thin:@//${DB_HOST:localhost}:${DB_PORT:1521}/FREEPDB1` | `FREEPDB1` is the default pluggable database in `gvenzl/oracle-free` |
| DB2 | `jdbc:db2://${DB_HOST:localhost}:${DB_PORT:50000}/${DB_NAME:operaton}` | Default database name — see OQ-3 |

### FR-4: Docker Compose database service

When `dockerCompose=true` AND `databaseOption` is non-H2, the generated `docker-compose.yml` includes a `db` service alongside the `app` service:

- `app` service sets `SPRING_PROFILES_ACTIVE=docker` and `depends_on: db` with `condition: service_healthy`
- `db` service uses the image below, exposes the standard port, sets credentials to match `application-docker.properties` defaults, and includes a `healthcheck`

| Database | Docker image | Notes |
|---|---|---|
| PostgreSQL | `postgres:16-alpine` | |
| MySQL | `mysql:8.4` | |
| MariaDB | `mariadb:11.4` | |
| MS SQL Server | `mcr.microsoft.com/mssql/server:2022-latest` | Generated project includes `docker/mssql-init.sql`, mounted via the compose entrypoint, which creates the `operaton_test` database and enables `READ_COMMITTED_SNAPSHOT ON` — the standard image does not auto-create user databases |
| Oracle | `gvenzl/oracle-free:latest` | First start ~2 min; see OQ-2 for healthcheck `start_period` |
| DB2 | `icr.io/db2_community/db2:latest` | See OQ-3 for mandatory env vars |

### FR-5: README database section

The generated `README.md` includes a **Database Setup** section covering three cases:

**H2 (default):** One-line note — H2 in-memory is active; `mvn spring-boot:run` is sufficient.

**Non-H2 + Docker Compose enabled:** `docker compose up` starts the `db` service (health-checked), then the application. Per-database notes:
- Oracle: `gvenzl/oracle-free` initializes on first start (~2 minutes); subsequent starts are fast
- DB2: `icr.io` may require a free IBM Cloud account; `LICENSE=accept` is pre-set in the generated compose file
- MS SQL Server: `docker/mssql-init.sql` runs automatically on first container start

**Non-H2 + no Docker Compose:** Step-by-step instructions including an installation link, a SQL snippet to create the schema and credentials, and the run command (`mvn spring-boot:run` — `local` profile is active by default). Override defaults via env vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

### FR-6: API, CLI, and MCP surface

**REST API (`/api/v1/generate`):** `databaseOption` field added to `ProjectConfigRequest`. Accepts enum string values case-insensitively; omitting defaults to `H2`. The `/api/v1/metadata` endpoint lists all database options with display labels.

Note: the Explorer path applies a server-side default of `databaseOption=POSTGRESQL` + `dockerCompose=true` when neither value is explicitly supplied — see FR-8.

**CLI (`npx operaton-starter`):** `--database <value>` flag. Interactive prompt inserted between project type and build system questions:
```
? Database:
❯ H2 (embedded, no setup required)
  PostgreSQL
  MySQL
  MariaDB
  Oracle
  MS SQL Server
  DB2
```
Non-interactive invocations with `--database` bypass the prompt.

**MCP (`generate_project` tool):** `database` parameter added with string enum schema. When a non-H2 database is specified and `dockerCompose` is false or omitted, the tool response includes:
```json
"warnings": ["Docker Compose not enabled; PostgreSQL service not configured. Pass dockerCompose: true to include it."]
```

### FR-7: Web UI — database selector

A database selector is added to the configuration form's "Infrastructure" section, visually grouped with the Docker Compose toggle.

- **Default:** H2
- **Non-H2 selected:** Docker Compose toggle enters a "Recommended" state (badge + tooltip: "Recommended: starts the database service alongside the application") without auto-enabling. The badge appears when a non-H2 database is selected and Docker Compose is off; it clears when the user enables Docker Compose or switches back to H2.
- Docker Compose toggle remains independently controllable at all times
- No blocking gates for Oracle/DB2 in the UI; all guidance lives in the generated README

### FR-8: Use case examples default to PostgreSQL + Docker Compose

Explorer path (project gallery) use case examples are rendered with `databaseOption=POSTGRESQL` and `dockerCompose=true` applied server-side when neither value is explicitly supplied by the caller, ensuring featured examples arrive with a realistic, runnable setup.

The override applies only to the Explorer path rendering logic. CLI and MCP invocations respect whatever `databaseOption` and `dockerCompose` values the caller passes, defaulting to `H2` / `false` per FR-1 if omitted. If a caller explicitly passes `databaseOption=H2` to the Explorer path, H2 is respected.

---

## NFRs

**NFR-1 — Generated project compiles and starts on first run.** For any valid `(databaseOption, dockerCompose)` combination: `mvn verify` (or Gradle equivalent) passes in a clean environment. For Docker Compose combinations: `docker compose up` starts the application connected to the database service.

**NFR-2 — H2 path unchanged.** Generated output for `databaseOption=H2` (or default) is byte-for-byte identical to current output.

**NFR-3 — Generation test matrix.** CI covers: H2 (all 3 build systems), PostgreSQL+compose, PostgreSQL no-compose, MySQL+compose, MSSQL+compose, Oracle+compose, DB2+compose. All combinations must pass `mvn verify` on the generated project.

**NFR-4 — Dependency versions current.** JDBC driver versions target the current stable release at time of template authoring and are eligible for automated Renovate/Dependabot updates.
