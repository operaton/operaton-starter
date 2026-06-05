---
baseline_commit: 8276b7e2ca9360e2a68b5e74f97a339c168e34f2
---

# Story 8.2: UC-02 Loan Application — DMN Decision + Service Tasks

## Status
todo

## Story

As a **developer evaluating Operaton's decision engine**,
I want a pre-built loan application example that combines DMN business rules with BPMN service tasks,
So that I can see decision-driven process branching using a stubbed external credit-score API.

## Acceptance Criteria

1. **Given** the Loan Application example project is generated and extracted **When** the developer runs `docker compose up -d && ./mvnw spring-boot:run` **Then** the application starts successfully; PostgreSQL and WireMock are both running; the application connects to both without errors

2. **Given** the `docker-compose.yml` **When** inspected **Then** it contains two services: a PostgreSQL service and a `wiremock/wiremock` service with a pinned minor version (e.g. `3.x.y`, not `3.x` or `latest`); each service has a health check; `depends_on: condition: service_healthy` is set for both; the Spring Boot app runs on the host, not in Docker

3. **Given** `src/main/resources/wiremock/mappings/` **When** inspected **Then** it contains at least one committed JSON stub file for the credit-score API; the `docker-compose.yml` mounts this directory into the WireMock container via a bind-mount; no WireMock stubs are configured in Java code

4. **Given** `src/main/resources/dmn/risk-assessment.dmn` **When** inspected **Then** it defines a decision table with inputs `creditScore` (integer) and `loanAmount` (integer), output `riskLevel` (string: `low`/`medium`/`high`), and hit policy `FIRST`; all three output values are reachable by distinct input combinations

5. **Given** the BPMN process `loan-application.bpmn` **When** inspected **Then** it models: `StartEvent → ServiceTask(credit score check) → BusinessRuleTask(risk assessment DMN) → ExclusiveGateway → [low] ServiceTask(auto-approve notify) → EndEvent / [medium] UserTask(underwriter review)[candidateGroups=underwriters] → EndEvent / [high] ServiceTask(auto-reject notify) → EndEvent`

6. **Given** the JUnit integration test **When** executed **Then** it uses `@ActiveProfiles("h2")` so CI requires no PostgreSQL; it covers all three DMN risk paths using parametrized test cases; the DMN table is also tested in isolation via `decisionService.evaluateDecisionByKey("risk-assessment")`; WireMock is started via Testcontainers; all assertions pass

7. **Given** the DMN engine capability **When** a developer adds `operaton-engine-dmn` to the project **Then** the dependency is explicitly declared in `pom.xml` / `build.gradle`; the build compiles and all DMN tests pass when the `operaton-spring-boot-starter-dmn` (or equivalent) starter is present; the dependency is not silently provided via transitive resolution

8. **Given** the project includes `src/main/resources/application-h2.properties` **When** the developer runs `./mvnw spring-boot:run --spring.profiles.active=h2` **Then** the application starts with embedded H2; WireMock is still required (external API stubs); no code changes are needed to switch the datasource

9. **Given** the generated README **When** read **Then** it includes a "Bootstrap Data" section, an embedded image of the `loan-application.bpmn` process model, and a `chmod +x mvnw` instruction for Mac/Linux users immediately before the first run command; the character-narrated section names jack and kate and walks through each DMN risk path

## Tasks/Subtasks

- [x] Task 1: Create BPMN template
  - [x] 1.1: Created `loan-application.bpmn.jte` — Start→ServiceTask(credit-score)→BusinessRuleTask(DMN)→Gateway→3 paths; includes BPMNShape/BPMNEdge layout

- [x] Task 2: Create DMN decision table template
  - [x] 2.1: Created `risk-assessment.dmn.jte` — inputs creditScore/loanAmount; output riskLevel; hit policy FIRST; 3 rules

- [x] Task 3: Create WireMock stub and Docker Compose templates (original — WireMock only)
  - [x] 3.1: Created `wiremock/mappings/credit-score-stub.json.jte`
  - [x] 3.2: Created `docker-compose.yml.jte` — single wiremock service (to be replaced in Task 9)

- [x] Task 4: Create data.sql and service task delegate templates
  - [x] 4.1: Created `data.sql.jte` — seeds eve/underwriters
  - [x] 4.2: Created `CreditScoreDelegate.java.jte`
  - [x] 4.3: Created `NotificationDelegate.java.jte`

- [x] Task 5: Create Maven pom.xml template
  - [x] 5.1: Created `maven/pom.xml.jte` — explicit operaton-spring-boot-starter-dmn + Testcontainers WireMock

- [x] Task 6: Create integration test template
  - [x] 6.1: Created `LoanApplicationIT.java.jte` — parametrized DMN paths; Testcontainers WireMock; isolated DMN evaluation test

- [x] Task 7: Create README template (original)
  - [x] 7.1: Created `README.md.jte`

- [x] Task 8: Register use case in MetadataController + wire generation engine
  - [x] 8.1–8.2: uc-02 entry added; GenerationEngine dispatch added

- [x] Task 9 (original): Validate — 45/45 tests passing

- [ ] Task 10: Update Docker Compose to include Postgres (FR70)
  - [ ] 10.1: Update `docker-compose.yml.jte` to add PostgreSQL 16 service alongside WireMock; both services have health checks; `depends_on` set for both
  - [ ] 10.2: Update `application.properties.jte` to use Postgres datasource by default

- [ ] Task 11: Add H2 fallback profile (FR74)
  - [ ] 11.1: Create `application-h2.properties.jte` — overrides datasource to H2 in-memory
  - [ ] 11.2: Add `@ActiveProfiles("h2")` to `LoanApplicationIT.java.jte`

- [ ] Task 12: Update README (FR71)
  - [ ] 12.1: Add "Bootstrap Data" section (users, admin account)
  - [ ] 12.2: Add embedded BPMN model image reference (`docs/loan-application.png`)
  - [ ] 12.3: Add `chmod +x mvnw` instruction before first run command
  - [ ] 12.4: Name jack and kate in character-narrated section

- [ ] Task 13: Run `mvn verify` — all tests green

## Dev Notes

- **Depends on Story 8.1** for `UseCaseExample` model, `useCaseExamples[]` in Metadata, and `useCaseId` in ProjectConfig. If implementing 8.1 and 8.2 in sequence, these are already in place.
- **DMN dependency**: `operaton-spring-boot-starter-dmn` must be declared explicitly in the generated pom.xml — do not rely on transitive inclusion.
- **WireMock version pinning**: Use `wiremock/wiremock:3.5.4` (or latest 3.x.y known stable). Never use `latest` or a `3.x` floating tag.
- **Testcontainers**: The integration test starts WireMock via `WireMockContainer` from `org.wiremock.integrations.testcontainers:wiremock-testcontainers-module`. The `docker-compose.yml` is for developer runtime only; tests use Testcontainers so CI doesn't need Docker Compose.
- **H2 profile**: Integration tests use `@ActiveProfiles("h2")`. Postgres compose is for developer runtime only.
- **WireMock stubs**: Must be committed JSON files under `src/main/resources/wiremock/mappings/` — no programmatic stub configuration in Java.

## Dev Agent Record

### Implementation Plan

1. Created JTE templates under `use-cases/uc-02-loan-application/`: BPMN, DMN decision table, WireMock stub JSON, docker-compose.yml (wiremock/wiremock:3.5.4 pinned), data.sql (eve/underwriters), CreditScoreDelegate, NotificationDelegate, pom.xml, LoanApplicationIT, README.
2. Refactored `GenerationEngine.generateUseCaseExample()` from monolithic to switch-based dispatch.
3. Added `uc-02-loan-application` entry to `MetadataController.buildUseCaseExamples()`.
4. Fixed JTE escaping: BPMN condition expressions use `${'$'}{...}` pattern.

### Completion Notes (original — Tasks 1–9)

All original tasks complete. 45/45 tests passing. Loan Application generates valid ZIP with loan-application.bpmn, dmn/risk-assessment.dmn, wiremock stubs, docker-compose.yml, and LoanApplicationIT.java.

Tasks 10–13 are new work added 2026-06-05 per PRD/Epics updates (FR70, FR74, FR71).

## File List

- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/loan-application.bpmn.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/risk-assessment.dmn.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/wiremock/mappings/credit-score-stub.json.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/docker-compose.yml.jte` — to be updated (add Postgres)
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/data.sql.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/application.properties.jte` — to be updated (Postgres datasource)
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/application-h2.properties.jte` — new (Task 11)
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/CreditScoreDelegate.java.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/NotificationDelegate.java.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/maven/pom.xml.jte` — to be updated (Postgres driver)
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/LoanApplicationIT.java.jte` — to be updated (@ActiveProfiles("h2"))
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/README.md.jte` — to be updated (Bootstrap Data, BPMN image, chmod+x, jack/kate narration)
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/MetadataController.java` — exists (uc-02 entry)
- `starter-server/src/test/java/org/operaton/dev/starter/server/ApiControllerTest.java` — 2 tests added

## Change Log

- 2026-06-05: Story created from Epic 8 for UC-02 Loan Application DMN + Service Tasks
- 2026-06-05: Story implemented by Dev Agent. All tasks complete, 45/45 tests passing, status → review
- 2026-06-05: Review fixes applied and verified, status → done
- 2026-06-05: Reopened (status → todo): PRD updates require Postgres in Docker Compose (FR70), H2 fallback profile (FR74), Bootstrap Data + BPMN image + chmod+x in README (FR71). Tasks 10–13 added.
