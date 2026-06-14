---
baseline_commit:
---

# Story 8.6: Implement Manual Refresh Endpoint and Diagnostics Actuator

## Status
ready-for-dev

## Story

As a maintainer,
I want a `POST /api/v1/examples/refresh` endpoint and a `/actuator/examples` diagnostics endpoint,
so that I can pick up new examples without restarting the server and can see exactly what loaded, from where, and at which SHA.

## Acceptance Criteria

1. **Given** a `POST /api/v1/examples/refresh` request **When** the controller runs **Then** `ExampleRepositoryLoader.load()` is re-invoked; the response is `200 OK` with a JSON body of per-source `Status` entries (`source`, `outcome`, `examplesLoaded`, `resolvedSha`, `lastFetchedAt`, `error?`).
2. **Given** a source fails during refresh while it previously had a successful snapshot in memory **When** the new `ExampleSnapshot` is assembled **Then** that source's slot is filled from the **previous** snapshot; the response `Status` for that source carries `outcome: stale:<reason>`.
3. **Given** the refresh endpoint is called repeatedly **When** each call completes **Then** the registry swap is atomic (an in-flight `GET /api/v1/metadata` cannot observe a torn snapshot — verified by a concurrency test).
4. **Given** `GET /actuator/examples` is invoked **When** the actuator endpoint serves the response **Then** the body matches the same `Status[]` shape returned by `/api/v1/examples/refresh`, reflecting the most recent load attempt.
5. **Given** the refresh endpoint is unauthenticated in v1 **When** a request hits it **Then** the controller serves it; a `[NOTE FOR PM]` comment in the controller references PRD Open Q-1 about unauthenticated access.

## Tasks/Subtasks

- [ ] Task 1: Define `ExampleSourceStatus` record: `source`, `outcome`, `examplesLoaded`, `resolvedSha`, `lastFetchedAt`, `error` (optional)
- [ ] Task 2: Extend `ExampleRepositoryLoader.load()` to return `List<ExampleSourceStatus>` and implement preserve-previous-on-failure merge
  - [ ] 2.1: On source failure during refresh, copy previous snapshot entry and mark `outcome: stale:<reason>`
  - [ ] 2.2: Store `List<ExampleSourceStatus>` in `ExampleSnapshot`
- [ ] Task 3: Implement `ExampleRefreshController` (`POST /api/v1/examples/refresh`)
  - [ ] 3.1: Invoke `ExampleRepositoryLoader.load()`; return `200 OK` with `List<ExampleSourceStatus>` JSON body
  - [ ] 3.2: Add `// [NOTE FOR PM]` comment referencing PRD Open Q-1 about unauthenticated access
- [ ] Task 4: Implement `ExampleSourcesEndpoint` as Spring Boot Actuator `@Endpoint(id = "examples")`
  - [ ] 4.1: `@ReadOperation` returns `List<ExampleSourceStatus>` from current `ExampleRegistry` snapshot
- [ ] Task 5: Write tests
  - [ ] 5.1: Concurrency test: concurrent `GET /api/v1/metadata` during `POST .../refresh` never observes torn snapshot
  - [ ] 5.2: Preserve-previous test: source fails on refresh, previous data still appears with `outcome: stale:...`
  - [ ] 5.3: Actuator test: `GET /actuator/examples` returns same shape as refresh response

## Dev Notes

- Architecture A5: Failed source on refresh preserves its previous in-memory snapshot. Per-source result merged into fresh `ExampleSnapshot` builder; missing/failed entries filled from previous snapshot.
- Architecture A7: `POST /api/v1/examples/refresh` — no auth v1, returns `ExampleSourceStatus[]`. `GET /actuator/examples` — actuator default auth.
- Architecture A13: `ExampleSourcesEndpoint` is the canonical diagnostics surface — add to its `Status` shape rather than introducing parallel logging-only diagnostics.
- Architecture A9: Refresh endpoint unauthenticated in v1 — document PRD Open Q-1 decision in controller.
- Follow existing Spring Boot Actuator `@Endpoint` / `@ReadOperation` pattern in the project.

### Project Structure Notes

- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleSourceStatus.java` — new record
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/api/ExampleRefreshController.java` — new
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/actuator/ExampleSourcesEndpoint.java` — new
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleRepositoryLoader.java` — extend (from Story 8.4)
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleSnapshot.java` — extend to carry `List<ExampleSourceStatus>`

### References

- [Source: docs/bmad/planning-artifacts/architecture.md#A5]
- [Source: docs/bmad/planning-artifacts/architecture.md#A7]
- [Source: docs/bmad/planning-artifacts/architecture.md#A9]
- [Source: docs/bmad/planning-artifacts/architecture.md#A13]

## Dev Agent Record

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
