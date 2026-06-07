---
baseline_commit: 8276b7e2ca9360e2a68b5e74f97a339c168e34f2
---

# Story 8.1: UC-01 Leave Request — HR Approval Workflow

## Status
in-progress

## Story

As a **developer new to Operaton**,
I want a pre-built leave request approval example I can run in under 2 minutes,
So that I immediately see how Operaton handles multi-role human task workflows using only the built-in Tasklist.

## Acceptance Criteria

1. **Given** the Leave Request example project is generated and extracted **When** the developer runs `docker compose up -d && ./mvnw spring-boot:run` **Then** the application starts successfully on port 8080 with PostgreSQL as the datasource; the Operaton Tasklist is accessible at `http://localhost:8080/operaton/app/tasklist`

2. **Given** the `docker-compose.yml` **When** inspected **Then** it contains a PostgreSQL service with a health check; `depends_on: condition: service_healthy` is set; the Spring Boot app runs on the host, not in Docker

3. **Given** the started application **When** a process instance is started **Then** a task appears in the Tasklist inbox of user `bob` (manager group); no other setup is needed to see the task

4. **Given** the generated `src/main/resources/data.sql` **When** inspected **Then** it seeds three users (`alice/alice`, `bob/bob`, `carol/carol`) into Operaton's identity tables and assigns them to groups `employees`, `managers`, and `hr` respectively; BPMN `candidateGroups` attributes match these group names exactly; an Operaton admin user is created if it does not already exist (the startup sequence checks and creates it on first boot)

5. **Given** the BPMN process `leave-request.bpmn` **When** inspected **Then** it models: `StartEvent → UserTask(manager reviews)[candidateGroups=managers] → ExclusiveGateway → [approved] UserTask(HR records)[candidateGroups=hr] → EndEvent / [rejected] UserTask(employee notified)[candidateGroups=employees] → EndEvent`; all flow elements include valid `BPMNShape`/`BPMNEdge` layout data

6. **Given** the JUnit integration test **When** executed **Then** it uses the H2 profile (`@ActiveProfiles("h2")` or `mvn test -Dspring.profiles.active=h2`); it includes an assertion verifying the process definition is deployed and the engine is reachable before any business-logic assertions; the test covers both the approval path (alice starts → bob approves → carol records) and the rejection path; all assertions pass without modification; zero active process instances remain after each path completes

7. **Given** the project includes `src/main/resources/application-h2.properties` **When** the developer runs `./mvnw spring-boot:run --spring.profiles.active=h2` **Then** the application starts with the embedded H2 datasource; no Docker Compose or PostgreSQL is required; no code changes are needed to switch profiles

8. **Given** the generated README **When** the developer reads the "Getting Started in 5 Minutes" section **Then** it: names alice and bob by name and gives step-by-step Tasklist instructions as those characters; includes a "Bootstrap Data" section explaining what `data.sql` seeds (users, groups, admin account) and how to re-apply it; includes an embedded image of the `leave-request.bpmn` process model; includes a `chmod +x mvnw` instruction for Mac/Linux users immediately before the first run command

## Tasks/Subtasks

- [x] Task 1: Create use case template directory and BPMN
  - [x] 1.1: Create `starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte` — full BPMN 2.0 with Start→UserTask(managers)→Gateway→[approved]UserTask(hr)→End/[rejected]UserTask(employees)→End; include BPMNShape/BPMNEdge layout; process id = `leave-request`
  - [x] 1.2: Create `src/main/resources/application.properties.jte` (shared common template reuse OK)

- [x] Task 2: Create data.sql template
  - [x] 2.1: Create `starter-templates/src/main/jte/use-cases/uc-01-leave-request/data.sql.jte` — seeds alice/bob/carol users + employees/managers/hr groups into Operaton identity tables (`ACT_ID_USER`, `ACT_ID_GROUP`, `ACT_ID_MEMBERSHIP`)

- [x] Task 3: Create Maven pom.xml template
  - [x] 3.1: Create `starter-templates/src/main/jte/use-cases/uc-01-leave-request/maven/pom.xml.jte` — Spring Boot + operaton-bpm-spring-boot-starter-webapp + H2 + operaton-bpm-junit5

- [x] Task 4: Create Application.java and integration test templates
  - [x] 4.1: Create `Application.java.jte` (can reuse/adapt process-application template)
  - [x] 4.2: Create `LeaveRequestIT.java.jte` — annotated with `@ActiveProfiles("h2")`; covers approval and rejection paths

- [x] Task 5: Create character-narrated README template (original)
  - [x] 5.1: Create `README.md.jte` — "Getting Started in 5 Minutes" section names alice and bob by name

- [x] Task 6: Register use case in MetadataController
  - [x] 6.1–6.3: UseCaseExample model, useCaseExamples in Metadata, openapi.yaml update

- [x] Task 7: Wire generation engine for use case
  - [x] 7.1–7.3: useCaseId in ProjectConfig; GenerationService dispatch; integration test

- [x] Task 8: Validate all tests pass (original)
  - [x] 8.1: Run `mvn verify` — 43/43 tests passing

- [ ] Task 9: Add PostgreSQL Docker Compose (FR70)
  - [ ] 9.1: Update `docker-compose.yml.jte` to include a PostgreSQL 16 service with health check (`pg_isready`); `depends_on: condition: service_healthy` on the app's compose service; Spring datasource properties point to `localhost:5432` with default Postgres credentials
  - [ ] 9.2: Update `application.properties.jte` to use `spring.datasource.url=jdbc:postgresql://localhost:5432/...` by default

- [ ] Task 10: Add H2 fallback profile (FR74)
  - [ ] 10.1: Create `application-h2.properties.jte` — overrides datasource to `jdbc:h2:mem:...`; includes Operaton H2 platform setting; annotate integration test with `@ActiveProfiles("h2")`
  - [ ] 10.2: Verify `./mvnw spring-boot:run --spring.profiles.active=h2` starts without Postgres

- [ ] Task 11: Add Operaton admin user creation (FR69)
  - [ ] 11.1: Update `data.sql.jte` to include INSERT-or-skip for an `admin` user in Operaton identity tables; ensure idempotency (MERGE or INSERT IF NOT EXISTS)

- [ ] Task 12: Update README template (FR71)
  - [ ] 12.1: Add "Bootstrap Data" section to `README.md.jte` explaining the seed data entries and how to re-apply `data.sql`
  - [ ] 12.2: Add embedded BPMN model image (reference the `leave-request.bpmn` diagram exported as PNG/SVG placed in `docs/` within the generated project, or link to a hosted image)
  - [ ] 12.3: Add `chmod +x mvnw` instruction for Mac/Linux users immediately before the first `./mvnw` command

- [ ] Task 13: Run `mvn verify` — all tests green

### Review Findings

- [x] [Review][Patch] Removed the added escalation path so the UC-01 BPMN matches the selected Story 8.1 review scope [starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte]
- [x] [Review][Patch] Updated README start-access guidance to match `candidateStarterGroups="employees,operaton-admin"` [starter-templates/src/main/jte/use-cases/uc-01-leave-request/README.md.jte:27]
- [x] [Review][Patch] Hard-coded PostgreSQL database name no longer matches the generated artifact id [starter-templates/src/main/jte/use-cases/uc-01-leave-request/docker-compose.yml.jte:7]
- [x] [Review][Patch] `remainingVacationDays` is overwritten during approval finalization, breaking the new history assertion [starter-templates/src/main/jte/use-cases/uc-01-leave-request/delegate/FinalizeLeaveApprovalDelegate.java.jte:27]
- [x] [Review][Patch] Removing the timer boundary path also removed the malformed `managerReviewTimeout` runtime risk [starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte]
- [x] [Review][Patch] `startDate.plusDays(durationDays - 1)` can overflow for extreme future dates [starter-templates/src/main/jte/use-cases/uc-01-leave-request/delegate/LeaveRequestValidationDelegate.java.jte:36-43]

## Dev Notes

- **Postgres service name in compose**: Use `postgres` as the service name; spring datasource URL `jdbc:postgresql://localhost:5432/${artifactId}` with `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` env vars.
- **H2 profile convention**: `application-h2.properties` activates the embedded H2 datasource. Integration tests always use `@ActiveProfiles("h2")` so CI doesn't require Docker.
- **Admin user idempotency**: Use `MERGE INTO ACT_ID_USER ... ON (ID_='admin')` (H2 syntax) or equivalent PostgreSQL `INSERT ... ON CONFLICT DO NOTHING` in a SQL script that is dialect-aware, or use a Spring `ApplicationRunner` bean that checks `identityService.createUserQuery().userId("admin").count() == 0` before creating.
- **BPMN image**: Generate a PNG/SVG export of the BPMN diagram and commit it under `docs/leave-request.png` in the template; README references it with a relative path `![Leave Request Process](docs/leave-request.png)`.
- **Template location convention**: Use cases live under `starter-templates/src/main/jte/use-cases/{useCaseId}/`.

## Dev Agent Record

### Implementation Plan

1. Created JTE templates under `use-cases/uc-01-leave-request/`: BPMN (full diagram with BPMNDi), data.sql (MERGE INTO H2 syntax for users/groups/memberships), maven/pom.xml, Application.java, application.properties, LeaveRequestIT.java, README.md.
2. Added `UseCaseExample` schema to `openapi.yaml` (+ `useCaseId` to `ProjectConfig` schema). Ran `mvn generate-sources` to produce generated model classes.
3. Added `useCaseExamples` list to `MetadataController.getMetadata()` with uc-01 entry.
4. Added `useCaseId` field to domain `ProjectConfig` record + Builder. Updated `ProjectConfigMapper` to pass it through.
5. Added `generateUseCaseExample()` to `GenerationEngine`; `generateCommonExtras` skips README for use cases (they own their README). Fixed duplicate-entry bug.
6. Added `metadata_returns_use_case_examples` and `generate_with_useCaseId_produces_zip_with_leave_request_bpmn_and_data_sql` tests to `ApiControllerTest`.

### Completion Notes (original — Tasks 1–8)

All 8 original tasks complete. 43/43 tests passing (37 starter-templates + 6 starter-server). Leave Request use case generates a valid ZIP with `leave-request.bpmn`, `data.sql`, `pom.xml`, and `LeaveRequestIT.java`. Metadata endpoint returns `useCaseExamples` array. Infrastructure added in this story (UseCaseExample model, useCaseId in ProjectConfig, generateUseCaseExample dispatch) serves as foundation for stories 8-2 through 8-5.

Tasks 9–13 are new work added 2026-06-05 per PRD/Epics updates (FR70, FR74, FR69, FR71).

## File List

- `openapi.yaml` — added `UseCaseExample` schema, `useCaseExamples` to Metadata schema, `useCaseId` to ProjectConfig schema
- `starter-server/src/main/resources/static/openapi.yaml` — synced from root
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/MetadataController.java` — added `buildUseCaseExamples()`, `setUseCaseExamples` call
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/ProjectConfigMapper.java` — maps `useCaseId` from DTO to domain
- `starter-server/src/test/java/org/operaton/dev/starter/server/ApiControllerTest.java` — 2 new tests
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/model/ProjectConfig.java` — added `useCaseId` field + Builder method
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/engine/GenerationEngine.java` — added `generateUseCaseExample()`, fixed README dedup
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/data.sql.jte` — new (to be updated: admin user + Postgres dialect)
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/maven/pom.xml.jte` — new (to be updated: Postgres driver dependency)
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/Application.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/application.properties.jte` — new (to be updated: Postgres datasource)
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/application-h2.properties.jte` — new (Task 10)
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/docker-compose.yml.jte` — new (Task 9)
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/LeaveRequestIT.java.jte` — updated (@ActiveProfiles("h2"))
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/README.md.jte` — updated (Bootstrap Data, BPMN image, chmod+x)

## Change Log

- 2026-06-05: Story created from Epic 8 for UC-01 Leave Request HR Approval Workflow
- 2026-06-05: Story implemented by Dev Agent. All tasks complete, 43/43 tests passing, status → review
- 2026-06-05: Review fixes applied and verified, status → done
- 2026-06-05: Reopened (status → todo): PRD updates require Postgres Docker Compose (FR70), H2 fallback profile (FR74), admin user auto-creation (FR69), Bootstrap Data + BPMN image + chmod+x in README (FR71). Tasks 9–13 added.
