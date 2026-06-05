---
baseline_commit: 8276b7e2ca9360e2a68b5e74f97a339c168e34f2
---

# Story 8.1: UC-01 Leave Request — HR Approval Workflow

## Status
done

## Story

As a **developer new to Operaton**,
I want a pre-built leave request approval example I can run in under 2 minutes,
So that I immediately see how Operaton handles multi-role human task workflows using only the built-in Tasklist.

## Acceptance Criteria

1. **Given** the Leave Request example project is generated and extracted **When** the developer runs `./mvnw spring-boot:run` **Then** the application starts successfully on port 8080 with no manual configuration; no Docker Compose is required; the Operaton Tasklist is accessible at `http://localhost:8080/operaton/app/tasklist`

2. **Given** the started application **When** a process instance is started **Then** a task appears in the Tasklist inbox of user `bob` (manager group); no other setup is needed to see the task

3. **Given** the generated `src/main/resources/data.sql` **When** inspected **Then** it seeds three users (`alice/alice`, `bob/bob`, `carol/carol`) into Operaton's identity tables and assigns them to groups `employees`, `managers`, and `hr` respectively; BPMN `candidateGroups` attributes match these group names exactly

4. **Given** the BPMN process `leave-request.bpmn` **When** inspected **Then** it models: `StartEvent → UserTask(manager reviews)[candidateGroups=managers] → ExclusiveGateway → [approved] UserTask(HR records)[candidateGroups=hr] → EndEvent / [rejected] UserTask(employee notified)[candidateGroups=employees] → EndEvent`; all flow elements include valid `BPMNShape`/`BPMNEdge` layout data

5. **Given** the Leave Request example project is generated and extracted **When** the developer runs `./mvnw spring-boot:run` **Then** no Docker Compose file exists in the project root; the example requires no external services

6. **Given** the JUnit integration test **When** executed **Then** it includes an assertion verifying the process definition is deployed and the engine is reachable before any business-logic assertions; the test covers both the approval path (alice starts → bob approves → carol records) and the rejection path; all assertions pass without modification; zero active process instances remain after each path completes

7. **Given** the generated README **When** the developer reads the "Getting Started in 5 Minutes" section **Then** it names alice and bob by name, describes the leave request scenario in plain language, and gives step-by-step instructions for logging into Tasklist as bob and completing the approval task — without referencing generic "User 1 / User 2" roles

## Tasks/Subtasks

- [x] Task 1: Create use case template directory and BPMN
  - [x] 1.1: Create `starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte` — full BPMN 2.0 with Start→UserTask(managers)→Gateway→[approved]UserTask(hr)→End/[rejected]UserTask(employees)→End; include BPMNShape/BPMNEdge layout; process id = `leave-request`
  - [x] 1.2: Create `src/main/resources/application.properties.jte` (shared common template reuse OK)

- [x] Task 2: Create data.sql template
  - [x] 2.1: Create `starter-templates/src/main/jte/use-cases/uc-01-leave-request/data.sql.jte` — seeds alice/bob/carol users + employees/managers/hr groups into Operaton identity tables (`ACT_ID_USER`, `ACT_ID_GROUP`, `ACT_ID_MEMBERSHIP`)

- [x] Task 3: Create Maven pom.xml template
  - [x] 3.1: Create `starter-templates/src/main/jte/use-cases/uc-01-leave-request/maven/pom.xml.jte` — Spring Boot + operaton-bpm-spring-boot-starter-webapp + H2 + operaton-bpm-junit5; no Docker Compose dependency

- [x] Task 4: Create Application.java and integration test templates
  - [x] 4.1: Create `Application.java.jte` (can reuse/adapt process-application template)
  - [x] 4.2: Create `LeaveRequestIT.java.jte` — Spring Boot integration test; verifies engine reachable; covers approval path (alice starts, bob claims+completes approved, carol records) and rejection path; uses `@SpringBootTest(webEnvironment=NONE)` + ProcessEngine injection; asserts zero active instances after each path

- [x] Task 5: Create character-narrated README template
  - [x] 5.1: Create `README.md.jte` — "Getting Started in 5 Minutes" section names alice and bob by name; describes leave request scenario; step-by-step Tasklist login instructions

- [x] Task 6: Register use case in MetadataController
  - [x] 6.1: Add `UseCaseExample` model class (or extend existing Metadata model) with fields: `useCaseId`, `title`, `description`, `tags`, `defaultConfig`
  - [x] 6.2: Add `useCaseExamples` list to `Metadata` response model and `MetadataController.getMetadata()` — include entry for `uc-01-leave-request`
  - [x] 6.3: Add `useCaseExamples` array to `openapi.yaml` TemplateManifest schema

- [x] Task 7: Wire generation engine for use case
  - [x] 7.1: Add `useCaseId` optional field to `ProjectConfig` model
  - [x] 7.2: In `GenerationService` (or equivalent), when `useCaseId=uc-01-leave-request`, generate leave-request template files in addition to base process-application files
  - [x] 7.3: Add integration test in `starter-server` asserting generation with `useCaseId=uc-01-leave-request` produces a ZIP containing `leave-request.bpmn` and `data.sql`

- [x] Task 8: Validate all tests pass
  - [x] 8.1: Run `mvn verify` from project root — all modules green (43/43 tests)

## Dev Notes

- **Template location convention**: Use cases live under `starter-templates/src/main/jte/use-cases/{useCaseId}/`. Each use case provides its own BPMN, data.sql, and integration test templates. Build files (pom.xml, build.gradle) live in `maven/` and `gradle-*/` subdirectories matching the existing convention.
- **Base project type for UC-01**: PROCESS_APPLICATION (Spring Boot embedded engine, `operaton-bpm-spring-boot-starter-webapp`). No Docker Compose needed.
- **Identity seeding in data.sql**: Operaton uses `ACT_ID_USER`, `ACT_ID_GROUP`, `ACT_ID_MEMBERSHIP` tables. Insert-or-ignore pattern: use `MERGE INTO` (H2) or `INSERT IGNORE`. Password field in `ACT_ID_USER` is the raw string (H2 dev mode; no hashing needed for examples).
- **BPMN layout**: Include `<bpmndi:BPMNDiagram>` section with BPMNShape/BPMNEdge for all elements so Operaton Cockpit renders it correctly.
- **Integration test pattern**: Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)` + inject `ProcessEngine`. Assert `repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave-request").count() == 1` before business assertions.
- **`useCaseId` in ProjectConfig**: Add as `Optional<String>` or nullable field. The generation controller passes it to the template engine. When present and matching a known use case, the engine generates use-case-specific files in addition to the base template set.
- **Metadata model extension**: The `Metadata` POJO lives in `starter-server/src/main/java/org/operaton/dev/starter/server/model/`. Add `List<UseCaseExample> useCaseExamples` field. The `UseCaseExample` record should have: `useCaseId` (String), `title` (String), `description` (String), `tags` (List<String>), `projectType` (String), `defaultArtifactId` (String).
- **openapi.yaml**: The static copy at `starter-server/src/main/resources/static/openapi.yaml` must be kept in sync with the Java model.

## Dev Agent Record

### Implementation Plan

1. Created JTE templates under `use-cases/uc-01-leave-request/`: BPMN (full diagram with BPMNDi), data.sql (MERGE INTO H2 syntax for users/groups/memberships), maven/pom.xml, Application.java, application.properties, LeaveRequestIT.java, README.md.
2. Added `UseCaseExample` schema to `openapi.yaml` (+ `useCaseId` to `ProjectConfig` schema). Ran `mvn generate-sources` to produce generated model classes.
3. Added `useCaseExamples` list to `MetadataController.getMetadata()` with uc-01 entry.
4. Added `useCaseId` field to domain `ProjectConfig` record + Builder. Updated `ProjectConfigMapper` to pass it through.
5. Added `generateUseCaseExample()` to `GenerationEngine`; `generateCommonExtras` skips README for use cases (they own their README). Fixed duplicate-entry bug.
6. Added `metadata_returns_use_case_examples` and `generate_with_useCaseId_produces_zip_with_leave_request_bpmn_and_data_sql` tests to `ApiControllerTest`.

### Completion Notes

All 8 tasks complete. 43/43 tests passing (37 starter-templates + 6 starter-server). Leave Request use case generates a valid ZIP with `leave-request.bpmn`, `data.sql`, `pom.xml`, and `LeaveRequestIT.java`. Metadata endpoint returns `useCaseExamples` array. Infrastructure added in this story (UseCaseExample model, useCaseId in ProjectConfig, generateUseCaseExample dispatch) serves as foundation for stories 8-2 through 8-5.

## File List

- `openapi.yaml` — added `UseCaseExample` schema, `useCaseExamples` to Metadata schema, `useCaseId` to ProjectConfig schema
- `starter-server/src/main/resources/static/openapi.yaml` — synced from root
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/MetadataController.java` — added `buildUseCaseExamples()`, `setUseCaseExamples` call
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/ProjectConfigMapper.java` — maps `useCaseId` from DTO to domain
- `starter-server/src/test/java/org/operaton/dev/starter/server/ApiControllerTest.java` — 2 new tests
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/model/ProjectConfig.java` — added `useCaseId` field + Builder method
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/engine/GenerationEngine.java` — added `generateUseCaseExample()`, fixed README dedup
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/data.sql.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/maven/pom.xml.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/Application.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/application.properties.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/LeaveRequestIT.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/README.md.jte` — new

## Change Log

- 2026-06-05: Story created from Epic 8 for UC-01 Leave Request HR Approval Workflow
- 2026-06-05: Story implemented by Dev Agent. All tasks complete, 43/43 tests passing, status → review
- 2026-06-05: Review fixes applied and verified, status → done
