---
baseline_commit:
---

# Story 8.7: Implement Examples Gallery UI (Search, Filters, Card, Download, Empty States)

## Status
ready-for-dev

## Story

As a developer evaluating Operaton,
I want a searchable, filterable Examples subsection in the gallery with rich cards and one-click ZIP download,
so that I can pick a runnable starter that fits my stack and have it on disk in under 30 seconds.

## Acceptance Criteria

1. **Given** `useExamples()`, `useGalleryFilters()`, and `useExampleDownload()` composables are implemented **When** `GalleryView.vue` mounts **Then** the gallery renders three subsections in order — **Project Types**, **Examples**, **Use Cases** — each with its own `<h2>` and short blurb; existing Project Types flows are unaffected.
2. **Given** `<GallerySearchBar>` is rendered **When** the user scrolls past the hero **Then** the search bar sticks below the app header with shadow, stays full-width; the input is `<input type="search" aria-label="Search examples and use cases">` with 200ms debounce; a `<span role="status" aria-live="polite" class="sr-only">` announces result counts after each debounced change.
3. **Given** the user toggles a filter chip **When** rendered **Then** chip is `<button type="button" aria-pressed="{bool}">` inside `<div role="toolbar" aria-label="Filter examples">`; Arrow Left/Right move focus; Space/Enter toggles; active chips show `×` glyph on hover/focus.
4. **Given** the active filter state **When** results are computed **Then** filter chips (runtime, buildSystem, complexity, integrations) apply only to Examples subsection; free-text search applies to both Examples and Use Cases; filters compose with AND across categories and OR within a category; Project Types are unaffected.
5. **Given** `<ExampleGalleryCard>` is rendered **When** an example carries an emoji `icon` **Then** it displays as plain text; when `icon` is a repo-relative image path, the image is fetched at the pinned SHA; when absent or load fails, a neutral default SVG glyph is shown — no error surfaced.
6. **Given** `<ExampleGalleryCard>` is rendered **When** the user clicks "More details" **Then** `aria-expanded` toggles; the panel reveals longDescription (markdown), integrations, bpmnConcepts, requires, authors, license, lastUpdated, and the pinned short SHA in mono `0.75rem` text; `Escape` closes the panel and returns focus to the disclosure button.
7. **Given** the user clicks "Download ZIP" **When** the request is in flight **Then** the button is disabled showing "Downloading…" with spinner; on success it is replaced for ~3s by "Downloaded {exampleId}.zip ✓"; on failure a card-error-inline block appears with a "Retry" affordance; the global `<ErrorBanner>` is **not** used for per-example failures.
8. **Given** the API returned `examples: []` **When** the Examples subsection renders **Then** `<ExamplesEmptyState>` shows "No examples are available right now…" with a "View format docs →" ghost button linking to `docs/examples-repository-format.md`.
9. **Given** active filters produce no matches **When** the Examples subsection renders **Then** `<ExamplesEmptyState>` shows "No examples match these filters." with a "Clear filters" ghost button that calls `useGalleryFilters().clear()`.
10. **Given** runtime / buildSystem / complexity Tags arrive from the API **When** rendered **Then** they route through `tagColors.ts` to the monochrome metadata-badge lane; `concept` and `integration` Tags continue to render with the existing accent tag lane — verified by component tests for each category.
11. **Given** the axe-core a11y CI job runs against `GalleryView.vue` **When** new components are in place **Then** zero violations are reported; suite covers sticky search bar, filter chip toolbar, card disclosure pattern, and empty states.

## Tasks/Subtasks

- [ ] Task 1: Implement composables in `starter-web/src/features/examples/`
  - [ ] 1.1: `useExamples.ts` — fetch `examples[]` from the generated `/api/v1/metadata` client; expose reactive list
  - [ ] 1.2: `useGalleryFilters.ts` — manage active filter chip state (runtime, buildSystem, complexity, integrations); AND-across-categories / OR-within-category logic; expose `clear()`
  - [ ] 1.3: `useExampleDownload.ts` — call `GET /api/v1/examples/{owner}/{repo}/{id}/download` via generated client; manage per-card state (idle / in-flight / success / error)
- [ ] Task 2: Implement `<GallerySearchBar>` component
  - [ ] 2.1: Sticky positioning (`position: sticky; top: var(--spacing-header-height)`) with scroll shadow
  - [ ] 2.2: `<input type="search" aria-label="Search examples and use cases">` with 200ms debounce
  - [ ] 2.3: Filter chip row (delegates to `<FilterChip>`)
  - [ ] 2.4: Live region `<span role="status" aria-live="polite" class="sr-only">` with count announcement
- [ ] Task 3: Implement `<FilterChip>` component
  - [ ] 3.1: `<button type="button" :aria-pressed="isActive">` inside `<div role="toolbar" aria-label="Filter examples">`
  - [ ] 3.2: Arrow Left/Right keyboard navigation; Space/Enter toggles; active chips show `×` glyph on hover/focus
- [ ] Task 4: Implement `<ExampleGalleryCard>` component
  - [ ] 4.1: Icon: emoji as plain text / repo-relative image at pinned SHA (`raw.githubusercontent.com/{owner}/{repo}/{sha}/{imagePath}`) / fallback SVG
  - [ ] 4.2: Short description, tag badges (runtime/buildSystem/complexity → monochrome lane; concept/integration → accent lane via `tagColors.ts`)
  - [ ] 4.3: "More details" disclosure: `aria-expanded` toggle; panel with markdown longDescription, integrations, bpmnConcepts, requires, authors, license, lastUpdated, short SHA; `Escape` closes and restores focus
  - [ ] 4.4: Download action: disabled+spinner in-flight; 3s inline success; error+Retry below action row; do NOT use global `<ErrorBanner>`
- [ ] Task 5: Implement `<ExamplesEmptyState>` component
  - [ ] 5.1: Empty-API variant: "No examples are available right now…" + "View format docs →" ghost button
  - [ ] 5.2: No-filter-match variant: "No examples match these filters." + "Clear filters" ghost button
- [ ] Task 6: Extend `GalleryView.vue`
  - [ ] 6.1: Order subsections: Project Types → Examples → Use Cases, each with `<h2>` and blurb
  - [ ] 6.2: Integrate `<GallerySearchBar>` above subsections; wire composables; pass filtered examples to `<ExampleGalleryCard>`
- [ ] Task 7: Extend `tagColors.ts` — add routing for `runtime`, `buildSystem`, `complexity` → monochrome metadata-badge lane
- [ ] Task 8: Write component tests: tag routing, card disclosure keyboard, filter chip keyboard, download state transitions, empty states, sticky search bar; ensure axe-core a11y CI passes

## Dev Notes

- Architecture A2: Frontend lives in `starter-web/src/features/examples/` (new) and `starter-web/src/views/GalleryView.vue` (extended).
- Architecture A13: Filter chip state is **not** mirrored to URL params in v1 — do not add URL query param syncing.
- Architecture A13: `examples[].lastUpdated` is author-asserted — display as-is, no GitHub commits API call.
- Architecture A10: Always route runtime/buildSystem/complexity through `tagColors.ts` — styling lane is a function of `TagCategory`, not a parallel data model.
- Architecture A4: Use only the generated TypeScript client for API calls — no hand-written fetch paths.
- Refer to `ux-designs/ux-operaton-starter-2026-05-31/DESIGN.md` and `EXPERIENCE.md` (revised 2026-06-13) for exact spacing, shadow, and color token names.
- The existing `<UseCaseGalleryCard>` is a reference implementation for the card disclosure pattern.
- Image at pinned SHA: `https://raw.githubusercontent.com/{owner}/{repo}/{sha}/{imagePath}` — always use registry SHA, never branch name.

### Project Structure Notes

- `starter-web/src/features/examples/useExamples.ts` — new
- `starter-web/src/features/examples/useGalleryFilters.ts` — new
- `starter-web/src/features/examples/useExampleDownload.ts` — new
- `starter-web/src/features/examples/ExampleGalleryCard.vue` — new
- `starter-web/src/features/examples/GallerySearchBar.vue` — new
- `starter-web/src/features/examples/FilterChip.vue` — new
- `starter-web/src/features/examples/ExamplesEmptyState.vue` — new
- `starter-web/src/features/examples/DownloadAction.vue` — new
- `starter-web/src/views/GalleryView.vue` — extend
- `starter-web/src/tagColors.ts` — extend with new TagCategory values

### References

- [Source: docs/bmad/planning-artifacts/architecture.md#A2]
- [Source: docs/bmad/planning-artifacts/architecture.md#A4]
- [Source: docs/bmad/planning-artifacts/architecture.md#A10]
- [Source: docs/bmad/planning-artifacts/architecture.md#A13]
- [Source: docs/bmad/planning-artifacts/ux-designs/ux-operaton-starter-2026-05-31/DESIGN.md]
- [Source: docs/bmad/planning-artifacts/ux-designs/ux-operaton-starter-2026-05-31/EXPERIENCE.md]

## Dev Agent Record

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
