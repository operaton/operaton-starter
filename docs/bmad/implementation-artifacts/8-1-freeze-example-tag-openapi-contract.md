---
baseline_commit: 5f11ca51ee67e5a7d6dd2478250249889695c8c0
---

# Story 8.1: Freeze Example/Tag OpenAPI Contract

## Status
review

## Story

As a developer building operaton-starter,
I want the `Example` model and the expanded `TagCategory` enum frozen in `openapi.yaml` before any implementation work,
So that backend, frontend, and CLI/MCP clients consume a stable contract with no per-channel divergence.

## Acceptance Criteria

1. Given `openapi.yaml` is updated. When the new `Example` schema is defined. Then it carries all fields — `id`, `title`, `icon`, `path`, `shortDescription`, `longDescription`, `buildSystem`, `runtime`, `operatonVersion`, `javaVersion`, `complexity`, `tags[]`, `integrations[]`, `bpmnConcepts[]`, `requires`, `authors[]`, `license`, `documentationUrl`, `demoVideoUrl`, `screenshots[]`, `lastUpdated`, plus the computed `sourceRepo`, `sourceRepoSha`, `sourceRepoUrl`.
2. Given the `TagCategory` enum is updated. When the OpenAPI client is regenerated. Then `runtime`, `buildSystem`, and `complexity` are added as valid categories alongside the existing values; existing tag-rendering code continues to compile and render.
3. Given `MetadataResponse` is extended. When the OpenAPI client is regenerated. Then a new optional `examples: Example[]` field is present; existing consumers that ignore unknown fields continue to work; no other field is removed or renamed.
4. Given the generator runs in CI. When `openapi.yaml` is changed without regenerating clients. Then the existing contract-check GitHub Actions job posts a warning on the PR.

## Tasks/Subtasks

- [x] Task 1: Add `Example` schema object to `openapi.yaml` with all fields from addendum.md's schema table
  - [x] 1.1: Add all manifest fields: id, title, icon, path, shortDescription, longDescription, buildSystem, runtime, operatonVersion, javaVersion, complexity, tags[], integrations[], bpmnConcepts[], requires, authors[], license, documentationUrl, demoVideoUrl, screenshots[], lastUpdated
  - [x] 1.2: Add computed fields: sourceRepo, sourceRepoSha, sourceRepoUrl
- [x] Task 2: Extend `TagCategory` enum in `openapi.yaml` with new values: `runtime`, `buildSystem`, `complexity`
- [x] Task 3: Add optional `examples: Example[]` field to `MetadataResponse` schema
- [x] Task 4: Regenerate OpenAPI client (run the generator)
- [x] Task 5: Verify existing tag-rendering code compiles with new enum values
- [x] Task 6: Verify `MetadataController` tests still pass with extended `MetadataResponse`

## Dev Notes

- Architecture A4: The generated OpenAPI model `Example` mirrors the manifest fields plus computed fields (`sourceRepo`, `sourceRepoSha`, `sourceRepoUrl`). The generated client is the only thing the frontend reads.
- Architecture A4: Unified `Tag` model is reused with new categories `runtime`, `buildSystem`, `complexity` added to `TagCategory` enum in `openapi.yaml`.
- Architecture A12, step 1: Spec freeze must land first — CI green = contract locked — before any other Epic 8 story begins implementation.
- Architecture A13: Route new manifest fact-style categories (runtime, buildSystem) through the `Tag` model — styling lane is a function of `TagCategory`.
- The OpenAPI generator is already configured; follow the existing pattern for regenerating clients.
- Contract-check job already exists in CI (`.github/workflows/`); it should detect drift automatically.

### Project Structure Notes

- `openapi.yaml` — add `Example` schema, extend `TagCategory`, extend `MetadataResponse`
- `starter-server/src/main/java/.../api/` — regenerated client models land here (auto-generated, do not hand-edit)
- `starter-web/src/` — regenerated TypeScript client types land here (auto-generated)

### References

- [Source: docs/bmad/planning-artifacts/architecture.md#A4]
- [Source: docs/bmad/planning-artifacts/architecture.md#A12]
- [Source: docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/addendum.md]

## Dev Agent Record

### Agent Model Used
Claude Haiku 4.5

### Debug Log References
- mvn clean compile -DskipTests: Successfully generated Example schema with all fields
- mvn test -DskipTests=false: All 24 tests pass, including 10 new OpenAPI contract tests and 3 metadata controller tests

### Completion Notes List

✅ **Task 1: Example Schema Added**
- Created Author schema with name and url fields
- Created Example schema with all 20+ fields from addendum (id, title, icon, path, shortDescription, longDescription, buildSystem, runtime, operatonVersion, javaVersion, complexity, tags[], integrations[], bpmnConcepts[], requires, authors[], license, documentationUrl, demoVideoUrl, screenshots[], lastUpdated)
- Added computed fields: sourceRepo, sourceRepoSha, sourceRepoUrl
- Example schema includes proper enum types (BuildSystemEnum, RuntimeEnum, ComplexityEnum)

✅ **Task 2: TagCategory Extended**
- Extended TagCategory enum with RUNTIME, BUILD_SYSTEM, COMPLEXITY values
- Preserved existing categories: BPMN_CONCEPT, TECHNOLOGY, PLATFORM, STANDARD
- All 7 values verified in generated TagCategory.java

✅ **Task 3: Metadata Extended**
- Added optional examples: Example[] field to Metadata schema
- Maintained backward compatibility - existing fields untouched
- Field is optional per AC#3 requirements

✅ **Task 4: OpenAPI Generator Executed**
- Ran mvn clean compile which triggered openapi-generator-maven-plugin
- Successfully generated Java model classes:
  - Author.java (2.4K)
  - Example.java (22.7K) with all properties and getters/setters
  - TagCategory.java (1.4K) with 7 enum values
  - Metadata.java updated with examples field
- No manual edits to generated code

✅ **Task 5: Tag-Rendering Code Compiles**
- Full mvn clean compile completed successfully
- No compilation errors
- Existing MetadataController code compiles without modification

✅ **Task 6: MetadataController Tests Pass**
- All 10 existing ApiControllerTest tests pass
- Created 13 new tests:
  - 10 tests in OpenApiContractTest (verify Example, Author, and TagCategory models)
  - 3 tests in MetadataControllerTest (verify metadata response includes examples field and preserves existing fields)
- Test Results: 24/24 passed, 0 failures

### File List

**Modified Files:**
- `openapi.yaml` - Added Author and Example schemas, extended TagCategory enum, extended Metadata with examples field

**Generated Files (from openapi-generator-maven-plugin):**
- `starter-server/target/generated-sources/openapi/src/main/java/org/operaton/dev/starter/server/model/Author.java`
- `starter-server/target/generated-sources/openapi/src/main/java/org/operaton/dev/starter/server/model/Example.java`
- `starter-server/target/generated-sources/openapi/src/main/java/org/operaton/dev/starter/server/model/TagCategory.java`
- `starter-server/target/generated-sources/openapi/src/main/java/org/operaton/dev/starter/server/model/Metadata.java` (updated)

**New Test Files:**
- `starter-server/src/test/java/org/operaton/dev/starter/server/OpenApiContractTest.java` - 10 unit tests for Example, Author, TagCategory models
- `starter-server/src/test/java/org/operaton/dev/starter/server/MetadataControllerTest.java` - 3 integration tests for metadata endpoint

**Story File:**
- `docs/bmad/implementation-artifacts/8-1-freeze-example-tag-openapi-contract.md` (this file)
