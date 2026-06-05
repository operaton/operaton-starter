---
baseline_commit: 8276b7e2ca9360e2a68b5e74f97a339c168e34f2
---

# Story 8.3: UC-03 Incident Management — Timer Boundary + Escalation

## Status
todo

## Story

As a **developer learning BPMN event handling**,
I want a pre-built incident management example with a timer boundary event that escalates unresolved tickets,
So that I can see how Operaton handles SLA enforcement and task escalation out of the box.

## Acceptance Criteria

1. **Given** the Incident Management example project is generated and extracted **When** the developer runs `docker compose up -d && ./mvnw spring-boot:run` **Then** the application starts successfully; PostgreSQL and WireMock are both running; a process instance can be started

2. **Given** the BPMN process `incident-management.bpmn` **When** inspected **Then** it models: `StartEvent → UserTask(first-line triage)[candidateGroups=first-line, BoundaryTimerEvent PT1H → escalate] → ExclusiveGateway → [resolved] ServiceTask(close ticket [REST]) → EndEvent / [timer fired] UserTask(second-line engineer)[candidateGroups=second-line] → ServiceTask(post-mortem notify [REST]) → EndEvent`

3. **Given** `src/main/resources/application-test.properties` **When** inspected **Then** it overrides the timer duration to `PT5S` so integration tests do not wait one hour for escalation to fire; the `test` Spring profile is activated automatically during `mvn test` — a developer must not add any manual configuration to run the test

4. **Given** the JUnit integration test for the escalation path **When** the timer is tested **Then** it uses `@ActiveProfiles("test")` (which activates the H2 datasource and PT5S timer); `ClockUtil.setCurrentTime(...)` advances Operaton's internal clock past the timer boundary; `Thread.sleep` is never used; `ClockUtil.reset()` is called in `@AfterEach`; the test asserts the task is reassigned to the `second-line` candidate group after escalation

5. **Given** `managementService.createJobQuery().timers()` **When** queried after process start **Then** exactly one timer job exists; this assertion appears in the test

6. **Given** the `docker-compose.yml` **When** inspected **Then** it contains two services: a PostgreSQL service and a `wiremock/wiremock` service with a pinned minor version (e.g. `3.x.y`, not `3.x` or `latest`); each service has a health check; `depends_on: condition: service_healthy` is set for both; the Spring Boot app runs on the host

7. **Given** the project includes `src/main/resources/application-h2.properties` **When** the developer runs `./mvnw spring-boot:run --spring.profiles.active=h2` **Then** the application starts with embedded H2; WireMock is still required for API stubs; no code changes are needed

8. **Given** the generated README **When** read **Then** it includes a "Bootstrap Data" section, an embedded image of the `incident-management.bpmn` process model, and a `chmod +x mvnw` instruction for Mac/Linux users immediately before the first run command; the character-narrated section names henry and iris and walks through both resolution and escalation paths

## Tasks/Subtasks

- [x] Task 1: Create BPMN template with timer boundary event
  - [x] 1.1: Created `incident-management.bpmn.jte` — interrupting BoundaryTimerEvent; full BPMNShape/BPMNEdge layout
  - [x] 1.2: Timer duration via `${timerDuration}` process variable; test profile overrides to PT5S

- [x] Task 2: Create application-test.properties template
  - [x] 2.1: Created `application-test.properties.jte` — sets timer to PT5S; maven-surefire-plugin activates `test` profile

- [x] Task 3: Create WireMock stubs and Docker Compose (original — WireMock only)
  - [x] 3.1: Created close-ticket-stub.json.jte
  - [x] 3.2: Created post-mortem-stub.json.jte
  - [x] 3.3: Created `docker-compose.yml.jte` — single WireMock service (to be updated in Task 10)

- [x] Task 4: Create data.sql and delegate templates
  - [x] 4.1: Created `data.sql.jte` — seeds frank/first-line and grace/second-line
  - [x] 4.2: Created `CloseTicketDelegate.java.jte`
  - [x] 4.3: Created `PostMortemDelegate.java.jte`

- [x] Task 5: Create Maven pom.xml template
  - [x] 5.1: Created `maven/pom.xml.jte` — maven-surefire activates test profile; Testcontainers WireMock

- [x] Task 6: Create integration test template
  - [x] 6.1: Created `IncidentManagementIT.java.jte` — ClockUtil timer advancement; ClockUtil.reset() @AfterEach

- [x] Task 7: Create README template (original)
  - [x] 7.1: Created `README.md.jte`

- [x] Task 8: Register use case in MetadataController + wire generation engine
  - [x] 8.1–8.2: uc-03 entry; GenerationEngine dispatch

- [x] Task 9 (original): Validate — 46/46 tests passing

- [ ] Task 10: Update Docker Compose to include Postgres (FR70)
  - [ ] 10.1: Update `docker-compose.yml.jte` to add PostgreSQL 16 service alongside WireMock; both services have health checks; `depends_on` set for both
  - [ ] 10.2: Update `application.properties.jte` to use Postgres datasource by default

- [ ] Task 11: Add H2 fallback profile (FR74)
  - [ ] 11.1: Create `application-h2.properties.jte` — overrides datasource to H2
  - [ ] 11.2: Consolidate `application-test.properties.jte` so the `test` profile also activates H2 (combine timer + H2 overrides in one properties file, or use Spring profile composition `test,h2`)
  - [ ] 11.3: Ensure `IncidentManagementIT.java.jte` uses `@ActiveProfiles("test")` which now covers both H2 and PT5S timer override

- [ ] Task 12: Update README (FR71)
  - [ ] 12.1: Add "Bootstrap Data" section
  - [ ] 12.2: Add embedded BPMN model image (`docs/incident-management.png`)
  - [ ] 12.3: Add `chmod +x mvnw` instruction before first run command
  - [ ] 12.4: Name henry and iris in character-narrated section; cover both resolution and escalation paths

- [ ] Task 13: Run `mvn verify` — all tests green

## Dev Notes

- **Depends on Story 8.1** for `UseCaseExample` model and `useCaseId` plumbing.
- **ClockUtil**: `org.operaton.bpm.engine.impl.util.ClockUtil` — call `ClockUtil.setCurrentTime(new Date(System.currentTimeMillis() + 10_000))` to advance past PT5S. Always reset in `@AfterEach`.
- **Test profile consolidation**: The `test` profile should activate both `timer.escalation.duration=PT5S` AND the H2 datasource. One `application-test.properties` handles both. No need for a separate `application-h2.properties` in the test path — `application-test.properties` is sufficient; the separate `application-h2.properties` is for developer runtime (`./mvnw spring-boot:run --spring.profiles.active=h2`).
- **WireMock stubs**: committed JSON under `src/main/resources/wiremock/mappings/`.
- **Postgres compose service name**: Use `postgres`; datasource URL `jdbc:postgresql://localhost:5432/${artifactId}`.

## Dev Agent Record

### Implementation Plan

1. Created BPMN with interrupting boundary timer using `${timerDuration}` process variable. JTE escaping: `${'$'}{timerDuration}`.
2. Created `application-test.properties.jte` with `timer.escalation.duration=PT5S`; pom.xml activates `test` profile via maven-surefire-plugin.
3. Created WireMock stubs and delegates.
4. Integration test uses `ClockUtil.setCurrentTime()` + `managementService.executeJob()` — no Thread.sleep.
5. Added UC-03 to MetadataController and GenerationEngine switch.

### Completion Notes (original — Tasks 1–9)

All original tasks complete. 46/46 tests passing. Incident Management generates valid ZIP with `incident-management.bpmn`, `application-test.properties`, WireMock stubs, delegates, and `IncidentManagementIT.java`.

Tasks 10–13 are new work added 2026-06-05 per PRD/Epics updates (FR70, FR74, FR71).

## File List

- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/incident-management.bpmn.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/application.properties.jte` — to be updated (Postgres datasource)
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/application-test.properties.jte` — to be updated (add H2 datasource override)
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/application-h2.properties.jte` — new (developer H2 runtime profile)
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/wiremock/mappings/close-ticket-stub.json.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/wiremock/mappings/post-mortem-stub.json.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/docker-compose.yml.jte` — to be updated (add Postgres)
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/data.sql.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/CloseTicketDelegate.java.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/PostMortemDelegate.java.jte` — exists
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/maven/pom.xml.jte` — to be updated (Postgres driver)
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/IncidentManagementIT.java.jte` — exists (@ActiveProfiles("test") already present)
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/README.md.jte` — to be updated (Bootstrap Data, BPMN image, chmod+x, henry/iris narration)

## Change Log

- 2026-06-05: Story created from Epic 8 for UC-03 Incident Management Timer Boundary Escalation
- 2026-06-05: Story implemented by Dev Agent. All tasks complete, 46/46 tests passing, status → review
- 2026-06-05: Review fixes applied and verified, status → done
- 2026-06-05: Reopened (status → todo): PRD updates require Postgres in Docker Compose (FR70), H2 fallback profile (FR74), Bootstrap Data + BPMN image + chmod+x in README (FR71). Tasks 10–13 added.
