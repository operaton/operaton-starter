---
baseline_commit: 8276b7e2ca9360e2a68b5e74f97a339c168e34f2
---

# Story 8.2: UC-02 Loan Application — DMN Decision + Service Tasks

## Status
done

## Story

As a **developer evaluating Operaton's decision engine**,
I want a pre-built loan application example that combines DMN business rules with BPMN service tasks,
So that I can see decision-driven process branching using a stubbed external credit-score API.

## Acceptance Criteria

1. **Given** the Loan Application example project is generated and extracted **When** the developer runs `docker compose up -d && ./mvnw spring-boot:run` **Then** the application starts successfully; WireMock is running and serving the credit-score stub; the application connects to WireMock without errors

2. **Given** the `docker-compose.yml` **When** inspected **Then** it contains exactly one service (`wiremock/wiremock`) with a pinned minor version (e.g. `3.x.y`, not `3.x` or `latest`); a health check on `/__admin/mappings` is defined; `depends_on` with `condition: service_healthy` is present for any service that depends on WireMock

3. **Given** `src/main/resources/wiremock/mappings/` **When** inspected **Then** it contains at least one committed JSON stub file for the credit-score API; the `docker-compose.yml` mounts this directory into the WireMock container via a bind-mount; no WireMock stubs are configured in Java code

4. **Given** `src/main/resources/dmn/risk-assessment.dmn` **When** inspected **Then** it defines a decision table with inputs `creditScore` (integer) and `loanAmount` (integer), output `riskLevel` (string: `low`/`medium`/`high`), and hit policy `FIRST`; all three output values are reachable by distinct input combinations

5. **Given** the BPMN process `loan-application.bpmn` **When** inspected **Then** it models: `StartEvent → ServiceTask(credit score check) → BusinessRuleTask(risk assessment DMN) → ExclusiveGateway → [low] ServiceTask(auto-approve notify) → EndEvent / [medium] UserTask(underwriter review)[candidateGroups=underwriters] → EndEvent / [high] ServiceTask(auto-reject notify) → EndEvent`

6. **Given** the JUnit integration test **When** executed **Then** it covers all three DMN risk paths using parametrized test cases; the DMN table is also tested in isolation via `decisionService.evaluateDecisionByKey("risk-assessment")`; WireMock is started via Testcontainers (not host Docker Compose) so the test runs in CI without Docker Compose support; all assertions pass

7. **Given** the DMN engine capability **When** a developer adds `operaton-engine-dmn` to the project **Then** the dependency is explicitly declared in `pom.xml` / `build.gradle`; the build compiles and all DMN tests pass when the `operaton-spring-boot-starter-dmn` (or equivalent) starter is present; the dependency is not silently provided via transitive resolution

## Tasks/Subtasks

- [x] Task 1: Create BPMN template
  - [x] 1.1: Create `starter-templates/src/main/jte/use-cases/uc-02-loan-application/loan-application.bpmn.jte` — models Start→ServiceTask(credit-score)→BusinessRuleTask(DMN)→Gateway→[low]ServiceTask(approve)→End/[medium]UserTask(underwriters)→End/[high]ServiceTask(reject)→End; includes BPMNShape/BPMNEdge layout

- [x] Task 2: Create DMN decision table template
  - [x] 2.1: Create `starter-templates/src/main/jte/use-cases/uc-02-loan-application/risk-assessment.dmn.jte` — DMN 1.3 decision table; inputs: creditScore (integer), loanAmount (integer); output: riskLevel (string low/medium/high); hit policy FIRST; three distinct rules covering all output values

- [x] Task 3: Create WireMock stub and Docker Compose templates
  - [x] 3.1: Create `wiremock/mappings/credit-score-stub.json.jte` — WireMock JSON mapping for GET /credit-score returning `{"score": 750}`
  - [x] 3.2: Create `docker-compose.yml.jte` — single `wiremock/wiremock:3.5.4` service with pinned version, health check on `/__admin/mappings`, bind-mount of `./src/main/resources/wiremock` into container

- [x] Task 4: Create data.sql and service task delegate templates
  - [x] 4.1: Create `data.sql.jte` — seeds one user `eve/eve` in group `underwriters`
  - [x] 4.2: Create `CreditScoreDelegate.java.jte` — JavaDelegate that calls WireMock REST endpoint, stores result in process variable `creditScore`
  - [x] 4.3: Create `NotificationDelegate.java.jte` — logs approval/rejection message

- [x] Task 5: Create Maven pom.xml template
  - [x] 5.1: Create `maven/pom.xml.jte` — Spring Boot + operaton-bpm-spring-boot-starter-webapp + operaton-spring-boot-starter-dmn (explicit) + H2 + Testcontainers WireMock + operaton-bpm-junit5

- [x] Task 6: Create integration test template
  - [x] 6.1: Create `LoanApplicationIT.java.jte` — `@SpringBootTest(webEnvironment=NONE)`; parametrized tests for low/medium/high DMN paths using Testcontainers WireMock; isolated DMN evaluation test via `decisionService.evaluateDecisionByKey("risk-assessment")`; all paths assert final state

- [x] Task 7: Create README template
  - [x] 7.1: Create `README.md.jte` — explains DMN + WireMock scenario; includes `docker compose up -d` prerequisite step; names personas by role

- [x] Task 8: Register use case in MetadataController
  - [x] 8.1: Add `uc-02-loan-application` entry to `useCaseExamples[]` in MetadataController (reuse model added in Story 8.1)
  - [x] 8.2: Wire generation engine: when `useCaseId=uc-02-loan-application`, include loan-application template files + docker-compose

- [x] Task 9: Run `mvn verify` — all modules green (45/45 tests)

## Dev Notes

- **Depends on Story 8.1** for `UseCaseExample` model, `useCaseExamples[]` in Metadata, and `useCaseId` in ProjectConfig. If implementing 8.1 and 8.2 in sequence, these are already in place.
- **DMN dependency**: `operaton-spring-boot-starter-dmn` must be declared explicitly in the generated pom.xml — do not rely on transitive inclusion.
- **WireMock version pinning**: Use `wiremock/wiremock:3.5.4` (or latest 3.x.y known stable). Never use `latest` or a `3.x` floating tag.
- **Testcontainers**: The integration test starts WireMock via `WireMockContainer` from `org.wiremock.integrations.testcontainers:wiremock-testcontainers-module`. The `docker-compose.yml` is for developer runtime only; tests use Testcontainers so CI doesn't need Docker Compose.
- **DMN file location**: Place as `src/main/resources/dmn/risk-assessment.dmn` in the generated project.
- **WireMock stubs**: Must be committed JSON files under `src/main/resources/wiremock/mappings/` — no programmatic stub configuration in Java.
- **Bind-mount path**: `docker-compose.yml` bind-mounts `./src/main/resources/wiremock:/home/wiremock` so edits to stubs don't require a container rebuild.

## Dev Agent Record

### Implementation Plan

1. Created JTE templates under `use-cases/uc-02-loan-application/`: BPMN (Start→CreditScoreService→DMN BusinessRuleTask→Gateway→3 paths), DMN decision table (FIRST hit policy, 3 rules for low/medium/high), WireMock stub JSON, docker-compose.yml (wiremock/wiremock:3.5.4 pinned), data.sql (eve/underwriters), CreditScoreDelegate, NotificationDelegate, pom.xml (with explicit operaton-spring-boot-starter-dmn + Testcontainers), LoanApplicationIT (parametrized + isolated DMN test), README.
2. Refactored `GenerationEngine.generateUseCaseExample()` from monolithic to switch-based dispatch (`generateUC01LeaveRequest` / `generateUC02LoanApplication`).
3. Added `uc-02-loan-application` entry to `MetadataController.buildUseCaseExamples()`.
4. Added 2 new tests to `ApiControllerTest`: ZIP content test and metadata presence test.
5. Fixed JTE escaping: BPMN condition expressions `${riskLevel == 'low'}` use `${'$'}{...}` pattern; Spring `@Value` uses `${"${...}"}` string literal pattern.

### Completion Notes

All 9 tasks complete. 45/45 tests passing (37 starter-templates + 8 starter-server). Loan Application use case generates a valid ZIP with `loan-application.bpmn`, `dmn/risk-assessment.dmn`, `wiremock/mappings/credit-score-stub.json`, `docker-compose.yml`, `pom.xml`, `CreditScoreDelegate.java`, `NotificationDelegate.java`, and `LoanApplicationIT.java`.

## File List

- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/loan-application.bpmn.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/risk-assessment.dmn.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/wiremock/mappings/credit-score-stub.json.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/docker-compose.yml.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/data.sql.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/Application.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/application.properties.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/CreditScoreDelegate.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/NotificationDelegate.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/maven/pom.xml.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/LoanApplicationIT.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/README.md.jte` — new
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/engine/GenerationEngine.java` — refactored dispatch, added generateUC02LoanApplication()
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/MetadataController.java` — added uc-02-loan-application use case entry
- `starter-server/src/test/java/org/operaton/dev/starter/server/ApiControllerTest.java` — 2 new tests

## Change Log

- 2026-06-05: Story created from Epic 8 for UC-02 Loan Application DMN + Service Tasks
- 2026-06-05: Story implemented by Dev Agent. All tasks complete, 45/45 tests passing, status → review
- 2026-06-05: Review fixes applied and verified, status → done
