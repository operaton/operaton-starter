---
baseline_commit: 8276b7e2ca9360e2a68b5e74f97a339c168e34f2
---

# Story 8.5: Use Case Example Gallery Cards

## Status
done

## Story

As a **developer browsing the gallery**,
I want to see use case example cards in the gallery's second section below the project types,
So that I can recognise a real-world scenario and jump straight to a pre-configured form without manual setup.

## Acceptance Criteria

1. **Given** the gallery landing page (`/`) **When** rendered **Then** a second section labelled (e.g.) "Use Case Examples" appears below the "Project Types" section; the section contains exactly four cards in MVP: Leave Request, Loan Application, Incident Management, Order Fulfillment

2. **Given** a use case example card **When** inspected visually **Then** it displays: a title, a one-sentence scenario description (e.g., "A manager approves employee leave — two roles, one process, zero infrastructure overhead"), and at least two capability tags from the set: `multi-role`, `docker-compose`, `DMN`, `timer`, `service-tasks`, `human-tasks`

3. **Given** a use case example card that requires Docker Compose **When** rendered **Then** a `docker-compose` tag is present and visually distinct from functional tags, signalling to the developer that an external service is required

4. **Given** a developer clicks a use case example card **When** navigated **Then** the details page (`/configure`) opens with the form pre-filled with the example's default values (project type, artifact ID, extras); the project type is shown as read-only context as per FR45; the `useCaseId` is passed to the generate endpoint so the server resolves the full parameter bundle server-side

5. **Given** the use case example card content **When** driven from the metadata endpoint **Then** the card title, description, tags, pre-fill values, and `useCaseId` are returned by `GET /api/v1/metadata` as part of the `useCaseExamples[]` array; no card content is hardcoded in the frontend

6. **Given** `POST /api/v1/generate` is called with an optional `useCaseId` parameter **When** a valid `useCaseId` is provided **Then** the server resolves the parameter bundle associated with that use case and uses it for generation; no client-side parameter expansion is required

## Tasks/Subtasks

- [x] Task 1: Verify/extend backend — `useCaseExamples[]` in metadata response
  - [x] 1.1: Confirm `GET /api/v1/metadata` returns `useCaseExamples[]` array (added in Stories 8.1–8.4); if not yet present, add `UseCaseExample` model and populate all 4 entries in `MetadataController`
  - [x] 1.2: Confirm `useCaseId` is accepted by `POST /api/v1/generate` and wired to generation engine; add if missing
  - [x] 1.3: Add `useCaseExamples` to `openapi.yaml` if not already present (array of `UseCaseExample` with `useCaseId`, `title`, `description`, `tags`, `projectType`, `defaultArtifactId`)

- [x] Task 2: Update TypeScript types
  - [x] 2.1: Add `UseCaseExample` interface to `starter-web/src/generated/types.ts`: `{ useCaseId: string; title: string; description: string; tags: string[]; projectType: string; defaultArtifactId: string }`
  - [x] 2.2: Add `useCaseExamples?: UseCaseExample[]` to `Metadata` interface
  - [x] 2.3: Add `useCaseId?: string` to `ProjectConfig` interface (for passing to generate endpoint)

- [x] Task 3: Create `UseCaseGalleryCard.vue` component
  - [x] 3.1: Accept `entry: UseCaseExample` prop
  - [x] 3.2: Render title, description, and capability tags
  - [x] 3.3: Style `docker-compose` tag visually distinct (e.g. amber/warning colour vs primary colour for functional tags)
  - [x] 3.4: Emit `select` event with `entry` on card click; keyboard accessible (tabindex, Enter/Space)

- [x] Task 4: Update `GalleryView.vue` to show use case section
  - [x] 4.1: Read `useCaseExamples` from metadata response
  - [x] 4.2: Render "Use Case Examples" section below "Project Types" section
  - [x] 4.3: Render four `UseCaseGalleryCard` components
  - [x] 4.4: On card click: navigate to `/configure` with query params `projectType={entry.projectType}&artifactId={entry.defaultArtifactId}&useCaseId={entry.useCaseId}`

- [x] Task 5: Update `ConfigureView.vue` to handle `useCaseId`
  - [x] 5.1: Read `useCaseId` from route query params
  - [x] 5.2: Pass `useCaseId` to the generate API call payload
  - [x] 5.3: When `useCaseId` present, show read-only project type banner (existing behaviour from Story 4.8) and pre-fill artifact ID from query param

- [x] Task 6: Write Vitest unit tests
  - [x] 6.1: Test `UseCaseGalleryCard` — renders title, description, tags; docker-compose tag has distinct class; click emits select
  - [x] 6.2: Test `GalleryView` — renders use case section when `useCaseExamples` present; card click navigates with correct query params
  - [x] 6.3: Run `npm run test:unit` — all tests pass

- [x] Task 7: Run full Maven build
  - [x] 7.1: Run `mvn verify` from project root — all modules green

## Dev Notes

- **Depends on Stories 8.1–8.4** for `useCaseExamples[]` to be populated in MetadataController and generation engine to handle `useCaseId`. If implementing in order, these will already be in place; Task 1.1–1.2 are verification steps.
- **GalleryView location**: `starter-web/src/views/GalleryView.vue`. It already renders project type cards from the metadata endpoint. Add a second section below the existing card grid.
- **ConfigureView location**: `starter-web/src/views/ConfigureView.vue` (or equivalent). Already handles `projectType` query param from Story 4.8. Extend to also read and forward `useCaseId`.
- **Tag styling**: Use Tailwind. Functional tags: `bg-primary/10 text-primary`. Docker-compose tag: `bg-amber-100 text-amber-700 border border-amber-300`. This visually signals infrastructure requirement.
- **Card navigation**: Use `router.push({ path: '/configure', query: { projectType, artifactId, useCaseId } })`. The configure view picks these up from `useRoute().query`.
- **No hardcoded frontend content**: All card data (title, description, tags) comes from the metadata endpoint. The frontend renders what the API returns.
- **`useCaseId` in generate payload**: The `ProjectConfig` sent to `POST /api/v1/generate` includes `useCaseId` as an optional string. The server uses it to resolve the parameter bundle and select the appropriate template files.
- **openapi.yaml sync**: Keep `starter-server/src/main/resources/static/openapi.yaml` in sync with any model changes.

## Dev Agent Record

### Implementation Plan

1. Added metadata-driven use case card rendering, keeping navigation and card content sourced from the backend.
2. Extended the use case metadata contract with default build and compose settings so the configure form can be pre-filled without hardcoded frontend data.
3. Wired `useCaseId` through query-based generation paths and tightened server-side use case defaults so generated archives stay consistent with each example.
4. Added and updated tests for gallery navigation, metadata shape, and query-driven example generation.

### Completion Notes

All story tasks are complete. The gallery now renders only the metadata-driven use case example section, the configure form receives use-case defaults from metadata, and both POST and query-driven generation paths accept `useCaseId`. Review fixes also hardened the use-case generators so Maven/build and Docker Compose defaults match the selected example.

## File List

- `openapi.yaml` — added query `useCaseId` and richer `UseCaseExample` metadata defaults
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/GenerateController.java` — query-based `useCaseId` support
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/MetadataController.java` — metadata-driven use case defaults
- `starter-server/src/test/java/org/operaton/dev/starter/server/ApiControllerTest.java` — query-based use-case generation coverage
- `starter-server/src/main/resources/static/openapi.yaml` — synced OpenAPI copy
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/engine/GenerationEngine.java` — applied server-side use case defaults and avoided duplicate common extras
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/model/ProjectConfig.java` — helper for use-case defaults
- `starter-web/src/generated/types.ts` — extended use case metadata types
- `starter-web/src/components/UseCaseGalleryCard.vue` — card emits selection events
- `starter-web/src/views/GalleryView.vue` — metadata-driven use case section and navigation
- `starter-web/src/views/__tests__/GalleryView.spec.ts` — gallery coverage for metadata-driven defaults
- `starter-web/src/components/__tests__/UseCaseGalleryCard.spec.ts` — use case card coverage

## Change Log

- 2026-06-05: Story created from Epic 8 for Use Case Example Gallery Cards
- 2026-06-05: Story implemented and review fixes applied, status → done
