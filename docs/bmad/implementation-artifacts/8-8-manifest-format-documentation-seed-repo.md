---
baseline_commit:
---

# Story 8.8: Author Manifest Format Documentation and Seed Sample Repository

## Status
ready-for-dev

## Story

As an example author,
I want clear documentation of the `.operaton-starter.yml` format and a working sample repository to copy from,
so that I can publish a new example without trial-and-error and without reading starter source code.

## Acceptance Criteria

1. **Given** `docs/examples-repository-format.md` is published **When** read end-to-end **Then** it covers: rationale paragraph, full field-by-field schema table matching `addendum.md`, annotated complete `.operaton-starter.yml` example, forward-compatibility contract (unknown fields ignored, `apiVersion` major-gated), repository layout expectations (manifest at root; each example in a subfolder referenced by `path:`), and registration instructions (PR to extend `starter.examples.repositories`, or env var `STARTER_EXAMPLES_REPOSITORIES`).
2. **Given** the main `README.md` is updated **When** read **Then** a "Contributing examples" section links to `docs/examples-repository-format.md`; the Examples Gallery section includes a "Publish your own examples →" link to the same doc.
3. **Given** the seed sample manifest at `operaton/operaton-examples` **When** committed to `main` **Then** it lists at least three examples covering the runtime matrix (Spring Boot + Maven, Quarkus + Gradle, plain-Java embedded), each with long description, emoji icon, tags spanning `runtime` / `buildSystem` / `complexity` / `concept` / `integration` categories, and `path:` pointing at a real subfolder.
4. **Given** the seed sample manifest validates against `ExampleManifestParser` **When** the loader fetches it at startup **Then** all examples load successfully (no `skipped:schema` outcomes); a smoke test runs the parser against the committed sample as a fixture.
5. **Given** the v1 release ships **When** an operator boots with default configuration **Then** the gallery displays seed examples without any environment override required.

## Tasks/Subtasks

- [ ] Task 1: Author `docs/examples-repository-format.md`
  - [ ] 1.1: Rationale paragraph explaining manifest-based approach and gallery purpose
  - [ ] 1.2: Full field-by-field schema table (adapt from `addendum.md`)
  - [ ] 1.3: Annotated complete `.operaton-starter.yml` example (adapt from PRD `sample-operaton-starter.yml`)
  - [ ] 1.4: Forward-compatibility contract section: unknown fields ignored, `apiVersion` major-gated
  - [ ] 1.5: Repository layout section: manifest at repo root, each example in subfolder referenced by `path:`
  - [ ] 1.6: Registration instructions: PR to extend `starter.examples.repositories`, or env var `STARTER_EXAMPLES_REPOSITORIES`
- [ ] Task 2: Update `README.md`
  - [ ] 2.1: Add "Contributing examples" section linking to `docs/examples-repository-format.md`
  - [ ] 2.2: Add "Publish your own examples →" link in Examples Gallery description
- [ ] Task 3: Commit seed `.operaton-starter.yml` to `operaton/operaton-examples` (external repo — coordinate separately)
  - [ ] 3.1: At minimum 3 examples: Spring Boot + Maven, Quarkus + Gradle, plain-Java embedded
  - [ ] 3.2: Each example: long description, emoji icon, tags covering runtime / buildSystem / complexity / concept / integration, `path:` to real subfolder
- [ ] Task 4: Add smoke test for `ExampleManifestParser` against the sample fixture
  - [ ] 4.1: Copy sample manifest as `starter-server/src/test/resources/fixtures/sample-operaton-starter.yml`
  - [ ] 4.2: `ExampleManifestParserSmokeTest` asserts all examples load without `ManifestRejected`
- [ ] Task 5: Verify `starter.examples.repositories=operaton/operaton-examples` is present in `application.properties` (set in Story 8.2)

## Dev Notes

- Architecture A4: `docs/examples-repository-format.md` is the **single source of truth** for the manifest schema. Java DTOs model it but do not duplicate the spec.
- Architecture A12, step 8: This is the final story in the Epic 8 sequence — it documents and validates the full feature.
- The `sample-operaton-starter.yml` in `docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/` is the starting point for both the annotated example in docs and the seed manifest.
- The `operaton/operaton-examples` repository is an external GitHub repository — the seed manifest commit is a separate action from the operaton-starter PR.
- For the smoke test, use a local fixture file (do not fetch from GitHub at test time) — keeps tests fast and reliable in CI.
- The "Publish your own examples →" link requires Story 8.7 (gallery UI) to be complete before it is visible on the deployed site.

### Project Structure Notes

- `docs/examples-repository-format.md` — new documentation file
- `README.md` — extend with "Contributing examples" section and gallery link
- `starter-server/src/test/resources/fixtures/sample-operaton-starter.yml` — new fixture
- `starter-server/src/test/java/.../examples/ExampleManifestParserSmokeTest.java` — new smoke test

### References

- [Source: docs/bmad/planning-artifacts/architecture.md#A4]
- [Source: docs/bmad/planning-artifacts/architecture.md#A12]
- [Source: docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/addendum.md]
- [Source: docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/sample-operaton-starter.yml]

## Dev Agent Record

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
