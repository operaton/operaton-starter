---
baseline_commit: 8276b7e2ca9360e2a68b5e74f97a339c168e34f2
---

# Story 8.4: UC-04 Order Fulfillment — Service Task Orchestration

## Status
done

## Story

As a **developer building service-oriented processes**,
I want a pre-built order fulfillment example with multiple service tasks calling stubbed REST APIs,
So that I can see how Operaton orchestrates multi-step external service calls with conditional routing.

## Acceptance Criteria

1. **Given** the Order Fulfillment example project is generated and extracted **When** the developer runs `docker compose up -d && ./mvnw spring-boot:run` **Then** the application starts; WireMock is serving inventory, payment, and notification stubs; a process instance can be started

2. **Given** the BPMN process `order-fulfillment.bpmn` **When** inspected **Then** it models: `StartEvent(order placed) → ServiceTask(validate inventory [REST]) → ExclusiveGateway → [in stock] ServiceTask(charge payment [REST]) → UserTask(pack & ship)[candidateGroups=warehouse] → ServiceTask(notify customer [REST]) → EndEvent(shipped) / [out of stock] ServiceTask(notify backorder [REST]) → EndEvent(backordered)`

3. **Given** `src/main/resources/wiremock/mappings/` **When** inspected **Then** it contains committed stub files for inventory (in-stock response), payment (success response), customer notification, and backorder notification; a second inventory stub with an out-of-stock response demonstrates the alternative path; no stubs are defined in Java code

4. **Given** the JUnit integration test **When** executed via Testcontainers **Then** it covers both the in-stock path (inventory → payment → human task → notify) and the out-of-stock path (inventory → backorder notify); WireMock container startup uses `waitFor(http("/__admin/mappings"))` before the first service task assertion; all assertions pass

5. **Given** the `docker-compose.yml` **When** inspected **Then** it contains exactly one service (`wiremock/wiremock`) with a pinned minor version (e.g. `3.x.y`, not `3.x` or `latest`); the `./wiremock` bind-mount path points to `src/main/resources/wiremock` so stub files are editable without rebuilding; health check and `depends_on` conditions are present

6. **Given** `data.sql` **When** inspected **Then** it seeds one user `dave/dave` in group `warehouse`; the warehouse UserTask `candidateGroups` attribute matches exactly

## Tasks/Subtasks

- [x] Task 1: Create BPMN template
  - [x] 1.1: Created `order-fulfillment.bpmn.jte` — 4 ServiceTasks (delegateExpression), 1 UserTask (warehouse), gateway with in-stock/out-of-stock paths, full BPMNDi layout

- [x] Task 2: Create WireMock stubs
  - [x] 2.1: `inventory-in-stock.json.jte` — GET /inventory/.* → available: true
  - [x] 2.2: `inventory-out-of-stock.json.jte` — GET /inventory/out-of-stock-.* → available: false
  - [x] 2.3: `payment-success.json.jte` — POST /payments → approved
  - [x] 2.4: `notify-customer.json.jte` — POST /notifications/customer → 200
  - [x] 2.5: `notify-backorder.json.jte` — POST /notifications/backorder → 200

- [x] Task 3: Create Docker Compose template
  - [x] 3.1: `docker-compose.yml.jte` — wiremock/wiremock:3.5.4, health check, bind-mount

- [x] Task 4: Create data.sql and 4 delegate templates
  - [x] 4.1: `data.sql.jte` — dave/warehouse
  - [x] 4.2: `InventoryDelegate.java.jte` — @Component("inventoryDelegate"), GET /inventory/{orderId}
  - [x] 4.3: `PaymentDelegate.java.jte` — @Component("paymentDelegate"), POST /payments
  - [x] 4.4: `NotifyCustomerDelegate.java.jte` — @Component("notifyCustomerDelegate")
  - [x] 4.5: `NotifyBackorderDelegate.java.jte` — @Component("notifyBackorderDelegate")

- [x] Task 5: Create Maven pom.xml template
  - [x] 5.1: `maven/pom.xml.jte` — Spring Boot + H2 + Testcontainers WireMock + operaton-bpm-junit5

- [x] Task 6: Create integration test template
  - [x] 6.1: `OrderFulfillmentIT.java.jte` — Testcontainers WireMock; in-stock path (warehouse task + complete) + out-of-stock path (direct end)

- [x] Task 7: Create README template
  - [x] 7.1: `README.md.jte` — explains orchestration; names dave; docker compose step

- [x] Task 8: Register use case in MetadataController
  - [x] 8.1: Added `uc-04-order-fulfillment` to buildUseCaseExamples()
  - [x] 8.2: Added generateUC04OrderFulfillment() to GenerationEngine switch

- [x] Task 9: Run `mvn verify` — all modules green (47/47 tests)

## Dev Notes

- **Depends on Story 8.1** for `UseCaseExample` model and `useCaseId` plumbing.
- **Multiple stubs for same endpoint**: For the in-stock/out-of-stock scenario, either use two separate stub mappings with different request matchers (e.g. different orderId values), or let the test switch WireMock stubs programmatically via the admin API. The committed stub files cover the developer-run scenario; tests swap stubs via Testcontainers WireMock admin.
- **Process variable for gateway**: `InventoryDelegate` stores `available` (boolean) in process variables. Gateway condition: `${available == true}` / `${available == false}`.
- **Testcontainers WireMock**: Use `WireMockContainer` from `org.wiremock.integrations.testcontainers`. The `waitFor(http("/__admin/mappings"))` strategy is the standard health check for Testcontainers WireMock.
- **4 service tasks**: Each gets its own JavaDelegate. Keep delegates thin — HTTP call via `RestTemplate` or `java.net.http.HttpClient` (no extra dependency needed with Spring Boot).

## Dev Agent Record

### Implementation Plan

1. BPMN uses `operaton:delegateExpression` with Spring bean names (`inventoryDelegate`, `paymentDelegate`, etc.). Each delegate is a `@Component` with the matching name.
2. Out-of-stock path uses WireMock URL pattern matching: orderId starting with `out-of-stock-` triggers the out-of-stock stub.
3. `InventoryDelegate` parses JSON response using Jackson (available via spring-boot-starter-web) to extract `available` boolean.
4. Added UC-04 to MetadataController list and GenerationEngine switch dispatch.
5. Test: in-stock path asserts warehouse task created + completes it; out-of-stock path starts process with `out-of-stock-` prefix orderId and asserts immediate completion.

### Completion Notes

All 9 tasks complete. 47/47 tests passing (37 starter-templates + 10 starter-server). Order Fulfillment use case generates a valid ZIP with `order-fulfillment.bpmn`, 5 WireMock stubs, 4 delegate classes, `docker-compose.yml`, and `OrderFulfillmentIT.java`.

## File List

- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/order-fulfillment.bpmn.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/wiremock/mappings/inventory-in-stock.json.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/wiremock/mappings/inventory-out-of-stock.json.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/wiremock/mappings/payment-success.json.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/wiremock/mappings/notify-customer.json.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/wiremock/mappings/notify-backorder.json.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/docker-compose.yml.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/data.sql.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/Application.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/application.properties.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/InventoryDelegate.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/PaymentDelegate.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/NotifyCustomerDelegate.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/NotifyBackorderDelegate.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/maven/pom.xml.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/OrderFulfillmentIT.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/README.md.jte` — new
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/engine/GenerationEngine.java` — added generateUC04OrderFulfillment()
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/MetadataController.java` — added uc-04-order-fulfillment
- `starter-server/src/test/java/org/operaton/dev/starter/server/ApiControllerTest.java` — 1 new test

## Change Log

- 2026-06-05: Story created from Epic 8 for UC-04 Order Fulfillment Service Task Orchestration
- 2026-06-05: Story implemented by Dev Agent. All tasks complete, 47/47 tests passing, status → review
- 2026-06-05: Review fixes applied and verified, status → done
