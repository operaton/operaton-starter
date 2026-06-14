---
baseline_commit:
---

# Story 8.4: Wire Up Registry, Startup Load, and Metadata Endpoint

## Status
ready-for-dev

## Story

As a developer building operaton-starter,
I want example manifests fetched in parallel at server startup, assembled into an immutable in-memory snapshot, and exposed through the existing `/api/v1/metadata` endpoint,
So that the frontend receives examples through the same contract it already consumes.

## Acceptance Criteria

1. Given an `ApplicationReadyEvent` fires. When `ExampleRepositoryLoader.load()` runs. Then it dispatches one parallel task per configured source via the fetcher + parser, assembles an `ExampleSnapshot`, and stores it atomically in `ExampleRegistry`; total wall time is <= 3 seconds for N <= 10 sources on a normal network.
2. Given a configured source fails (any cause). When the loader completes. Then the source is recorded in the snapshot with an `outcome` of `skipped:<reason>` and zero examples; the application starts successfully; other sources are unaffected.
3. Given every configured source fails at startup. When the loader completes. Then the application still starts; `examples[]` in `/api/v1/metadata` is an empty array; no `ErrorBanner`-equivalent server-side flag is raised.
4. Given `MetadataController` serves `GET /api/v1/metadata`. When at least one source loaded successfully. Then the response includes `examples[]` with each entry carrying the manifest fields plus `sourceRepo`, `sourceRepoSha`, and `sourceRepoUrl` (HTML URL to the example folder at the pinned SHA); `useCaseExamples` and `projectTypes` are unchanged in shape.
5. Given the contract test for `MetadataResponse`. When it runs against the live response. Then the response validates against the regenerated OpenAPI schema with zero violations.

## Tasks/Subtasks

- [ ] Task 1: Implement `ExampleSnapshot` immutable record
  - [ ] 1.1: Per-source state: `source`, `outcome`, `examples: List<Example>`, `resolvedSha`
- [ ] Task 2: Implement `ExampleRegistry` with `AtomicReference<ExampleSnapshot>` for thread-safe swap
- [ ] Task 3: Implement `ExampleRepositoryLoader` as `@Service` with `ApplicationReadyEvent` listener
  - [ ] 3.1: Dispatch one parallel fetch+parse task per configured source (use `CompletableFuture` or virtual threads per project pattern)
  - [ ] 3.2: Collect results into `ExampleSnapshot`; on any source failure record `skipped:<reason>` and continue
  - [ ] 3.3: Call `ExampleRegistry.swap(snapshot)` atomically
  - [ ] 3.4: Log summary at INFO level: sources loaded, total examples, any skipped sources
- [ ] Task 4: Extend `MetadataController.getMetadata()` to read from `ExampleRegistry` and include `examples[]` in response
  - [ ] 4.1: Compute `sourceRepoUrl` as HTML URL `https://github.com/{owner}/{repo}/tree/{sha}/{examplePath}`
- [ ] Task 5: Add contract test that validates the live `MetadataResponse` against the OpenAPI schema
- [ ] Task 6: Ensure `ExampleRegistry` is initialized with an empty snapshot at bean creation (avoids NPE before load completes)

## Dev Notes

- Architecture A5: `ExampleRepositoryLoader.load()` dispatches parallel per-source tasks; assembles `ExampleSnapshot` (immutable record); stores via `ExampleRegistry.swap(snapshot)` (atomic reference, readers never see torn state).
- Architecture A5: Failure model is per source, never global. Failure outcomes: `skipped:network`, `skipped:http-<status>`, `skipped:schema`, `skipped:api-version`.
- Architecture A7: `GET /api/v1/metadata` is backwards-compatible — `examples[]` is additive only.
- Architecture A10: `ExamplesPackageBoundaryTest` — `org.operaton.dev.starter.server.examples` may NOT depend on `org.operaton.dev.starter.templates`.
- Architecture A8: Add `ExamplesPackageBoundaryTest` ArchUnit test to enforce the examples<->templates boundary.
- The `sourceRepoUrl` is the HTML GitHub URL to the example's subfolder at the pinned SHA: `https://github.com/{owner}/{repo}/tree/{sha}/{example.path}`.
- Pre-Story-8.1 must be done (OpenAPI contract frozen) before merging this story.
- New classes in `org.operaton.dev.starter.server.examples` package.

### Project Structure Notes

- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleSnapshot.java` — new record
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleRegistry.java` — new (AtomicReference holder)
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleRepositoryLoader.java` — new @Service
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/MetadataController.java` — extend to include examples[]
- `starter-server/src/test/java/.../arch/ExamplesPackageBoundaryTest.java` — new ArchUnit test
- `starter-server/src/test/java/.../api/MetadataControllerTest.java` — extend with examples[] contract test

### References

- [Source: docs/bmad/planning-artifacts/architecture.md#A5]
- [Source: docs/bmad/planning-artifacts/architecture.md#A7]
- [Source: docs/bmad/planning-artifacts/architecture.md#A8]
- [Source: docs/bmad/planning-artifacts/architecture.md#A10]

## Dev Agent Record

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
