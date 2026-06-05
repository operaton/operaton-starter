---
baseline_commit: 8276b7e2ca9360e2a68b5e74f97a339c168e34f2
---

# Story 8.3: UC-03 Incident Management — Timer Boundary + Escalation

## Status
done

## Story

As a **developer learning BPMN event handling**,
I want a pre-built incident management example with a timer boundary event that escalates unresolved tickets,
So that I can see how Operaton handles SLA enforcement and task escalation out of the box.

## Acceptance Criteria

1. **Given** the Incident Management example project is generated and extracted **When** the developer runs `docker compose up -d && ./mvnw spring-boot:run` **Then** the application starts successfully; WireMock is available; a process instance can be started

2. **Given** the BPMN process `incident-management.bpmn` **When** inspected **Then** it models: `StartEvent → UserTask(first-line triage)[candidateGroups=first-line, BoundaryTimerEvent PT1H → escalate] → ExclusiveGateway → [resolved] ServiceTask(close ticket [REST]) → EndEvent / [timer fired] UserTask(second-line engineer)[candidateGroups=second-line] → ServiceTask(post-mortem notify [REST]) → EndEvent`

3. **Given** `src/main/resources/application-test.properties` **When** inspected **Then** it overrides the timer duration to `PT5S` so integration tests do not wait one hour for escalation to fire; the `test` Spring profile is activated automatically during `mvn test` (e.g. via `maven-surefire-plugin` `systemPropertyVariables` or `@ActiveProfiles` on the test class) — a developer must not add any manual configuration to run the test

4. **Given** the JUnit integration test for the escalation path **When** the timer is tested **Then** `ClockUtil.setCurrentTime(...)` advances Operaton's internal clock past the timer boundary; `Thread.sleep` is never used for timer advancement; `ClockUtil.reset()` is called in `@AfterEach` to prevent test pollution; the test asserts the task is reassigned to the `second-line` candidate group after escalation

5. **Given** `managementService.createJobQuery().timers()` **When** queried after process start **Then** exactly one timer job exists (confirms timer registration); this assertion appears in the test

6. **Given** the `docker-compose.yml` **When** inspected **Then** it contains exactly one service (`wiremock/wiremock`) with a pinned minor version (e.g. `3.x.y`, not `3.x` or `latest`); health check and `depends_on: condition: service_healthy` are present

## Tasks/Subtasks

- [x] Task 1: Create BPMN template with timer boundary event
  - [x] 1.1: Create `starter-templates/src/main/jte/use-cases/uc-03-incident-management/incident-management.bpmn.jte` — interrupting BoundaryTimerEvent on first-line UserTask; escalation path leads to second-line UserTask; full BPMNShape/BPMNEdge layout
  - [x] 1.2: Timer duration parameterized via process variable `timerDuration` (injected at start); test profile overrides to PT5S

- [x] Task 2: Create application-test.properties template
  - [x] 2.1: Create `application-test.properties.jte` — sets `timer.escalation.duration=PT5S`; maven-surefire-plugin activates `test` profile via `systemPropertyVariables` in `pom.xml.jte`

- [x] Task 3: Create WireMock stubs and Docker Compose
  - [x] 3.1: Create `wiremock/mappings/close-ticket-stub.json.jte` — POST /tickets/{id}/close stub returning 200
  - [x] 3.2: Create `wiremock/mappings/post-mortem-stub.json.jte` — POST /notifications/post-mortem stub returning 200
  - [x] 3.3: Create `docker-compose.yml.jte` — wiremock/wiremock:3.5.4 with health check, bind-mount

- [x] Task 4: Create data.sql and delegate templates
  - [x] 4.1: Create `data.sql.jte` — seeds frank/first-line and grace/second-line
  - [x] 4.2: Create `CloseTicketDelegate.java.jte` — calls REST close-ticket endpoint
  - [x] 4.3: Create `PostMortemDelegate.java.jte` — calls REST post-mortem notification endpoint

- [x] Task 5: Create Maven pom.xml template
  - [x] 5.1: Create `maven/pom.xml.jte` — maven-surefire-plugin with spring.profiles.active=test; Testcontainers WireMock

- [x] Task 6: Create integration test template
  - [x] 6.1: Create `IncidentManagementIT.java.jte` — @ActiveProfiles("test"); Testcontainers WireMock; ClockUtil.setCurrentTime() for timer advancement; ClockUtil.reset() in @AfterEach; asserts 1 timer job; covers resolved + escalation paths

- [x] Task 7: Create README template
  - [x] 7.1: Create `README.md.jte` — explains timer escalation; names frank/grace; includes docker compose step

- [x] Task 8: Register use case in MetadataController
  - [x] 8.1: Add `uc-03-incident-management` entry to `useCaseExamples[]`
  - [x] 8.2: Wire generation engine for `useCaseId=uc-03-incident-management`

- [x] Task 9: Run `mvn verify` — all modules green (46/46 tests)

## Dev Notes

- **Depends on Story 8.1** for `UseCaseExample` model and `useCaseId` plumbing.
- **Timer in BPMN**: Use `<timeDuration>${'$'}{timerDuration}</timeDuration>` with a process variable or Spring expression. The simplest approach for Spring integration: use `operaton:inputParameter` to inject `${timer.escalation.duration}` from Spring properties into the process variable on start, then reference it in the timer definition.
- **ClockUtil**: `org.operaton.bpm.engine.impl.util.ClockUtil` — call `ClockUtil.setCurrentTime(new Date(System.currentTimeMillis() + 10_000))` to advance past PT5S. Always reset in `@AfterEach`.
- **Non-interrupting vs interrupting**: Use an **interrupting** boundary timer for escalation (interrupts the first-line task when timer fires). This simplifies the test since the first-line task is cancelled.
- **WireMock stubs**: committed JSON under `src/main/resources/wiremock/mappings/`.
- **Profile activation in surefire**: Add to pom.xml.jte `<plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><configuration><systemPropertyVariables><spring.profiles.active>test</spring.profiles.active></systemPropertyVariables></configuration></plugin>`

## Dev Agent Record

### Implementation Plan

1. Created BPMN with interrupting boundary timer using `${timerDuration}` process variable for flexible duration. JTE escaping: `${'$'}{timerDuration}` outputs the literal `${timerDuration}`.
2. Created `application-test.properties.jte` with `timer.escalation.duration=PT5S`; pom.xml activates `test` profile via maven-surefire-plugin.
3. Created WireMock stubs for close-ticket (POST /tickets/{id}/close) and post-mortem (POST /notifications/post-mortem).
4. Created delegates injecting base URL via `@Value`; dynamic path constructed at runtime.
5. Integration test uses `ClockUtil.setCurrentTime()` + `managementService.executeJob()` — no Thread.sleep.
6. Added UC-03 to MetadataController and GenerationEngine switch.

### Completion Notes

All 9 tasks complete. 46/46 tests passing (37 starter-templates + 9 starter-server). Incident Management use case generates a valid ZIP with `incident-management.bpmn`, `application-test.properties`, WireMock stubs, delegates, and `IncidentManagementIT.java`.

## File List

- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/incident-management.bpmn.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/application.properties.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/application-test.properties.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/wiremock/mappings/close-ticket-stub.json.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/wiremock/mappings/post-mortem-stub.json.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/docker-compose.yml.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/data.sql.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/Application.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/CloseTicketDelegate.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/PostMortemDelegate.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/maven/pom.xml.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/IncidentManagementIT.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/README.md.jte` — new
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/engine/GenerationEngine.java` — added generateUC03IncidentManagement()
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/MetadataController.java` — added uc-03-incident-management
- `starter-server/src/test/java/org/operaton/dev/starter/server/ApiControllerTest.java` — 1 new test

## Change Log

- 2026-06-05: Story created from Epic 8 for UC-03 Incident Management Timer Boundary Escalation
- 2026-06-05: Story implemented by Dev Agent. All tasks complete, 46/46 tests passing, status → review
- 2026-06-05: Review fixes applied and verified, status → done
