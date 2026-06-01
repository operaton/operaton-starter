---
baseline_commit: 63e1a76a62d421936a5bcad69596371c124781e2
---

# Story 4.8: Conditional Form Rendering & Gallery-to-Form Context Handoff

## Status
done

## Story

As a **developer arriving from the project gallery**,
I want the configuration details page to display my pre-selected project type as read-only context and hide options that don't apply,
So that the form is focused, unambiguous, and never presents irrelevant configuration choices.

## Acceptance Criteria

1. **Given** a user clicks a gallery card (e.g. "Process Application") **When** they land on `/configure` **Then** the project type is displayed as a read-only context banner (e.g. "Configuring: Process Application") at the top of the form — not as an editable field; a "← Change project type" link navigates back to `/`

2. **Given** the project type is pre-set from a gallery selection **When** the form renders **Then** the project type field is absent from the editable form fields entirely — no disabled selector, no greyed-out option; only the read-only banner appears

3. **Given** a user navigating directly to `/configure` without a projectType query param **When** the form renders **Then** the project type IS presented as an editable selector (current behavior — Practitioner path unchanged)

4. **Given** `buildSystem` selection on the form **When** a user interacts with it **Then** it is a two-step control: first a choice between Maven and Gradle; if Gradle is chosen, a DSL sub-option (Groovy DSL / Kotlin DSL) appears; the DSL sub-option is hidden when Maven is selected; the sub-option must be selected before generation is enabled (FR10)

5. **Given** `projectType=PROCESS_APPLICATION` is pre-set or selected **When** the form renders **Then** the `deploymentTarget` selector is hidden entirely; `dockerCompose` and `githubActions` are visible

6. **Given** `projectType=PROCESS_ARCHIVE` is pre-set or selected **When** the form renders **Then** the `deploymentTarget` selector is visible and required; `githubActions` toggle is hidden entirely

7. **Given** the user navigates back to the gallery and selects a different project type **When** they arrive on `/configure` with the new type **Then** the visible option set updates correctly; all other previously entered field values are preserved where they still apply

8. **Given** a shareable URL encoding a `projectType` query parameter **When** the form loads **Then** conditional rendering rules apply as if the user arrived from the gallery

9. **Given** a Vitest unit test for the conditional rendering logic **When** the test runs **Then** it covers: PROCESS_APPLICATION hides deploymentTarget and shows githubActions; PROCESS_ARCHIVE shows deploymentTarget and hides githubActions; build system two-step switching works correctly

## Tasks/Subtasks

- [x] Task 1: Add "from gallery" detection to useProjectForm composable
  - [x] 1.1: Detect if `projectType` is set via query param (indicates gallery arrival or shareable link)
  - [x] 1.2: Export `isProjectTypeFromQuery: ComputedRef<boolean>` from `useProjectForm`
  - [x] 1.3: Add `visibleOptions` computed: returns which fields should be visible given current `form.projectType`
  - [x] 1.4: Expose `buildSystemCategory: Ref<'maven'|'gradle'|null>` and `gradleDsl: Ref<'GRADLE_GROOVY'|'GRADLE_KOTLIN'|null>` for two-step build selection; sync to `form.buildSystem`

- [x] Task 2: Update ConfigureView.vue — read-only project type banner
  - [x] 2.1: When `isProjectTypeFromQuery` is true, replace the project type radio group with a read-only banner showing the project type display name
  - [x] 2.2: Add "← Change project type" link in the banner that navigates to `/`
  - [x] 2.3: When `isProjectTypeFromQuery` is false, show editable project type selector as before

- [x] Task 3: Update ConfigureView.vue — two-step build system selection (FR10)
  - [x] 3.1: Replace single build system radio group with: step-1 "Maven / Gradle" choice, then step-2 Gradle DSL sub-option that appears only when Gradle is selected
  - [x] 3.2: When Maven is selected, hide the DSL sub-option entirely; set `form.buildSystem = 'MAVEN'`
  - [x] 3.3: When Gradle is selected, show DSL sub-option (Groovy DSL / Kotlin DSL); generation button disabled until DSL chosen
  - [x] 3.4: Initialize two-step state correctly from URL query params

- [x] Task 4: Update ConfigureView.vue — conditional option visibility
  - [x] 4.1: Hide `deploymentTarget` entirely when `projectType !== 'PROCESS_ARCHIVE'`
  - [x] 4.2: Hide `githubActions` toggle entirely when `projectType === 'PROCESS_ARCHIVE'`
  - [x] 4.3: Use `v-if` (not `v-show`) for proper DOM removal — no hidden elements in the form

- [x] Task 5: Write Vitest unit tests
  - [x] 5.1: Test `useProjectForm` composable: visibleOptions for PROCESS_APPLICATION vs PROCESS_ARCHIVE
  - [x] 5.2: Test two-step build system: Maven selection → buildSystem='MAVEN', DSL hidden; Gradle → DSL shown
  - [x] 5.3: Test isProjectTypeFromQuery detection
  - [x] 5.4: Run `npm run test:unit` in `starter-web` and confirm all pass

## Dev Notes

- `ConfigureView.vue` current state: project type uses radio group iterating `metadata?.projectTypes`; build system uses radio group over `metadata?.buildSystems`; `deploymentTarget` uses `v-if="form.projectType === 'PROCESS_ARCHIVE'"` already
- `useProjectForm.ts` exports: `form`, `errors`, `isValid`, `initFromQuery`; add the new exports alongside
- The "from gallery" detection: if `route.query.projectType` is present, the user came from gallery (or shareable link) — treat them the same way per FR45
- Two-step build system: `buildSystemCategory` is an intermediate reactive ref that maps to `form.buildSystem`; when category='maven' → `form.buildSystem='MAVEN'`; when category='gradle' + `gradleDsl` set → `form.buildSystem=gradleDsl`
- `DMN_PROJECT` project type exists in `useProjectForm.ts` constants but is not tested here; ensure the conditional logic doesn't break for DMN_PROJECT (treat as non-PROCESS_ARCHIVE for conditional purposes)
- Use `v-if` not `v-show` for conditional rendering (FR46 says "hidden entirely" — DOM removal, not CSS hide)
- Keep existing ARIA labels and help icons intact; add help for the new two-step build system control
- GalleryView already navigates to `/configure?projectType=...` when a card is clicked (check `GalleryView.vue` to confirm this)

## Dev Agent Record

### Implementation Plan

1. Extended `useProjectForm.ts` with `buildSystemCategory` and `gradleDsl` refs for two-step build selection, synced via watcher to `form.buildSystem`. Added `isProjectTypeFromQuery` ref set to true when `initFromQuery` receives a valid `projectType`. Validation updated to require DSL when gradle category is selected.

2. Updated `ConfigureView.vue` to destructure new exports; added `projectTypeDisplayName` computed; replaced project type radio group with v-if/v-else for read-only banner (gallery path) vs editable selector (practitioner path); replaced build system single radio with two-step control (Maven/Gradle → DSL sub-option); added `v-if="form.projectType !== 'PROCESS_ARCHIVE'"` guard around githubActions toggle.

3. Created `useProjectForm.spec.ts` with 13 tests covering all three feature areas. All 29 tests (13 new + 16 existing) pass.

### Debug Log

- ESLint output garbled by rtk proxy; proceeded based on clean test run and no syntax errors.

### Completion Notes

All 5 tasks complete. 29/29 Vitest tests pass. ConfigureView.vue and useProjectForm.ts updated per all ACs. Story ready for code review.

## File List

- `starter-web/src/composables/useProjectForm.ts` — modified: added buildSystemCategory, gradleDsl, isProjectTypeFromQuery; updated validation and initFromQuery
- `starter-web/src/views/ConfigureView.vue` — modified: read-only banner, two-step build selection, conditional githubActions visibility
- `starter-web/src/composables/__tests__/useProjectForm.spec.ts` — created: 13 unit tests for FR10, FR45, FR46

## Change Log

- 2026-05-31: Story implemented by Dev Agent. All tasks complete, tests passing, status → review.

### Review Findings

- [x] [Review][Patch] `PROCESS_ARCHIVE` now hides the GitHub Actions control but still keeps `githubActions=true` from the default form state, so archive generations continue to emit `.github/workflows/ci.yml` with no way for the user to turn it off. [`starter-web/src/views/ConfigureView.vue:277`]
