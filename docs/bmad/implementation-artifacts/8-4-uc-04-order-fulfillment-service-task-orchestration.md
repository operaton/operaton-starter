---
baseline_commit: 8276b7e2ca9360e2a68b5e74f97a339c168e34f2
---

# Story 8.4: UC-04 Order Fulfillment — Service Task Orchestration

## Status
todo

## Story

As a **developer building service-oriented processes**,
I want a pre-built order fulfillment example with multiple service tasks calling stubbed REST APIs,
So that I can see how Operaton orchestrates multi-step external service calls with conditional routing.

## Acceptance Criteria

1. **Given** the Order Fulfillment example project is generated and extracted **When** the developer runs `docker compose up -d && ./mvnw spring-boot:run` **Then** the application starts; PostgreSQL and WireMock are both running and serving inventory, payment, and notification stubs; a process instance can be started

2. **Given** the BPMN process `order-fulfillment.bpmn` **When** inspected **Then** it models: `StartEvent(order placed) → ServiceTask(validate inventory [REST]) → ExclusiveGateway → [in stock] ServiceTask(charge payment [REST]) → UserTask(pack & ship)[candidateGroups=warehouse] → ServiceTask(notify customer [REST]) → EndEvent(shipped) / [out of stock] ServiceTask(notify backorder [REST]) → EndEvent(backordered)`

3. **Given** `src/main/resources/wiremock/mappings/` **When** inspected **Then** it contains committed stub files for inventory (in-stock response), payment (success response), customer notification, and backorder notification; a second inventory stub with an out-of-stock response demonstrates the alternative path; no stubs are defined in Java code

4. **Given** the JUnit integration test **When** executed via Testcontainers **Then** it uses `@ActiveProfiles("h2")` so CI requires no PostgreSQL; it covers both the in-stock path (inventory → payment → human task → notify) and the out-of-stock path (inventory → backorder notify); WireMock container startup uses `waitFor(http("/__admin/mappings"))` before the first service task assertion; all assertions pass

5. **Given** the `docker-compose.yml` **When** inspected **Then** it contains two services: a PostgreSQL service and a `wiremock/wiremock` service with a pinned minor version (e.g. `3.x.y`, not `3.x` or `latest`); the `./wiremock` bind-mount path points to `src/main/resources/wiremock`; health checks and `depends_on: condition: service_healthy` are present for both; the Spring Boot app runs on the host

6. **Given** `data.sql` **When** inspected **Then** it seeds one user `dave/dave` in group `warehouse`; an Operaton admin user is created at startup if absent; the warehouse UserTask `candidateGroups` attribute matches exactly

7. **Given** the project includes `src/main/resources/application-h2.properties` **When** the developer runs `./mvnw spring-boot:run --spring.profiles.active=h2` **Then** the application starts with embedded H2; WireMock is still required for API stubs; no code changes are needed to switch the datasource

8. **Given** the generated README **When** read **Then** it includes a "Bootstrap Data" section, an embedded image of the `order-fulfillment.bpmn` process model, and a `chmod +x mvnw` instruction for Mac/Linux users immediately before the first run command; the character-narrated section names dave and walks through both the in-stock and out-of-stock paths

## Tasks/Subtasks

- [x] Task 1: Create BPMN template
  - [x] 1.1: Created `order-fulfillment.bpmn.jte` — 4 ServiceTasks, 1 UserTask (warehouse), gateway with in-stock/out-of-stock paths, full BPMNDi layout

- [x] Task 2: Create WireMock stubs
  - [x] 2.1–2.5: Created inventory-in-stock, inventory-out-of-stock, payment-success, notify-customer, notify-backorder stubs

- [x] Task 3: Create Docker Compose template (original — WireMock only)
  - [x] 3.1: Created `docker-compose.yml.jte` — single WireMock service (to be updated in Task 10)

- [x] Task 4: Create data.sql and 4 delegate templates
  - [x] 4.1: Created `data.sql.jte` — dave/warehouse
  - [x] 4.2–4.5: Created InventoryDelegate, PaymentDelegate, NotifyCustomerDelegate, NotifyBackorderDelegate

- [x] Task 5: Create Maven pom.xml template
  - [x] 5.1: Created `maven/pom.xml.jte` — Spring Boot + H2 + Testcontainers WireMock + operaton-bpm-junit5

- [x] Task 6: Create integration test template
  - [x] 6.1: Created `OrderFulfillmentIT.java.jte` — Testcontainers WireMock; in-stock and out-of-stock paths

- [x] Task 7: Create README template (original)
  - [x] 7.1: Created `README.md.jte`

- [x] Task 8: Register use case in MetadataController + wire generation engine
  - [x] 8.1–8.2: uc-04 entry; GenerationEngine dispatch

- [x] Task 9 (original): Validate — 47/47 tests passing

- [ ] Task 10: Update Docker Compose to include Postgres (FR70)
  - [ ] 10.1: Update `docker-compose.yml.jte` to add PostgreSQL 16 service alongside WireMock; both services have health checks; `depends_on` set for both
  - [ ] 10.2: Update `application.properties.jte` to use Postgres datasource by default

- [ ] Task 11: Add H2 fallback profile (FR74)
  - [ ] 11.1: Create `application-h2.properties.jte` — overrides datasource to H2 in-memory
  - [ ] 11.2: Add `@ActiveProfiles("h2")` to `OrderFulfillmentIT.java.jte`

- [ ] Task 12: Add Operaton admin user creation (FR69)
  - [ ] 12.1: Update `data.sql.jte` to include INSERT-or-skip for admin user; ensure idempotency

- [ ] Task 13: Update README (FR71)
  - [ ] 13.1: Add "Bootstrap Data" section (dave/warehouse + admin account)
  - [ ] 13.2: Add embedded BPMN model image (`docs/order-fulfillment.png`)
  - [ ] 13.3: Add `chmod +x mvnw` instruction before first run command
  - [ ] 13.4: Name dave in character-narrated section; cover both in-stock and out-of-stock paths

- [ ] Task 14: Run `mvn verify` — all tests green

## Dev Notes

- **Depends on Story 8.1** for `UseCaseExample` model and `useCaseId` plumbing.
- **Multiple stubs for same endpoint**: For in-stock/out-of-stock, use two separate stub mappings with different request matchers (different orderId values), or swap stubs via Testcontainers WireMock admin in tests.
- **Process variable for gateway**: `InventoryDelegate` stores `available` (boolean). Gateway condition: `${available == true}` / `${available == false}`.
- **Testcontainers WireMock**: Use `WireMockContainer` from `org.wiremock.integrations.testcontainers`. `waitFor(http("/__admin/mappings"))` is the standard health check.
- **H2 profile**: Integration tests use `@ActiveProfiles("h2")`. Postgres compose is for developer runtime only.
- **Admin user idempotency**: Same pattern as Story 8.1 — check existence before insert.

## Dev Agent Record

### Implementation Plan

1. BPMN uses `operaton:delegateExpression` with Spring bean names. Each delegate is a `@Component`.
2. Out-of-stock path: orderId starting with `out-of-stock-` triggers the out-of-stock stub.
3. `InventoryDelegate` parses JSON response using Jackson to extract `available` boolean.
4. Added UC-04 to MetadataController list and GenerationEngine switch dispatch.

### Completion Notes (original — Tasks 1–9)

All original tasks complete. 47/47 tests passing. Order Fulfillment generates valid ZIP with `order-fulfillment.bpmn`, 5 WireMock stubs, 4 delegates, `docker-compose.yml`, and `OrderFulfillmentIT.java`.

Tasks 10–14 are new work added 2026-06-05 per PRD/Epics updates (FR70, FR74, FR69, FR71).

## File List

- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/order-fulfillment.bpmn.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/wiremock/mappings/*.json.jte` — exist (5 stubs)
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/docker-compose.yml.jte` — to be updated (add Postgres)
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/data.sql.jte` — to be updated (admin user)
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/application.properties.jte` — to be updated (Postgres datasource)
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/application-h2.properties.jte` — new (Task 11)
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/InventoryDelegate.java.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/PaymentDelegate.java.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/NotifyCustomerDelegate.java.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/NotifyBackorderDelegate.java.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/maven/pom.xml.jte` — to be updated (Postgres driver)
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/OrderFulfillmentIT.java.jte` — to be updated (@ActiveProfiles("h2"))
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/README.md.jte` — to be updated (Bootstrap Data, BPMN image, chmod+x, dave narration)

## Change Log

- 2026-06-05: Story created from Epic 8 for UC-04 Order Fulfillment Service Task Orchestration
- 2026-06-05: Story implemented by Dev Agent. All tasks complete, 47/47 tests passing, status → review
- 2026-06-05: Review fixes applied and verified, status → done
- 2026-06-05: Reopened (status → todo): PRD updates require Postgres in Docker Compose (FR70), H2 fallback profile (FR74), admin user creation (FR69), Bootstrap Data + BPMN image + chmod+x in README (FR71). Tasks 10–14 added.
