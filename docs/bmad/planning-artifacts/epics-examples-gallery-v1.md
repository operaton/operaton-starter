---
stepsCompleted: ["step-01-validate-prerequisites", "step-02-design-epics"]
inputDocuments: 
  - "docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/prd.md"
epicStructure:
  totalEpics: 4
  approvedDate: "2026-06-15"
prerequisiteTask:
  - "examples-gallery-ui-spec.md (pre-sprint design review, blocks Epic 2 & 3 story creation)"
---

# Examples Gallery v1 - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Examples Gallery v1, decomposing the requirements from the Examples Gallery PRD into implementable stories organized by user value and technical delivery.

## Requirements Inventory

### Functional Requirements

**FR Group A — Manifest Schema & Repository Layout**
- FR-A1: Define a `.operaton-starter.yml` manifest format placed at the root of an example repository.
- FR-A2: The manifest declares a list of examples; each example references a subdirectory via `path:` (relative to repo root).
- FR-A3: Each example entry **MUST** provide: `id` (slug, unique within repo), `title`, `shortDescription` (≤ 200 chars), `path`.
- FR-A4: Each example entry **MAY** provide: longDescription (markdown), authors, license, documentationUrl, demoVideoUrl, buildSystem (maven|gradle), runtime (spring-boot|quarkus|plain-java|other), operatonVersion, javaVersion, requires, tags, integrations, bpmnConcepts, complexity (beginner|intermediate|advanced), icon (emoji or SVG/PNG ≤ 64×64), screenshots, lastUpdated (ISO date).
- FR-A5: A `repository` block at manifest root **MAY** provide registration metadata: `name`, `description`, `maintainer`.
- FR-A6: Unknown fields are ignored, not rejected — the schema is forward-compatible.
- FR-A7: Manifest version is declared via `apiVersion: operaton-starter/v1` at the top; loaders refuse manifests with unknown major versions and log a warning.

**FR Group B — Repository Registration & Loading**
- FR-B1: The starter holds a **maintainer-controlled** list of example-repository sources. v1 sources are configuration properties, not a UI surface.
- FR-B2: Each registered source is a GitHub repository identifier of the form `owner/repo[@ref]`. If `@ref` is omitted, the repo's default branch is used.
- FR-B3: v1 ships with **`operaton/operaton-examples`** preconfigured.
- FR-B4: Repository sources are configured via Spring properties (`starter.examples.repositories[]`) and overridable via `STARTER_EXAMPLES_REPOSITORIES` env var (comma-separated).
- FR-B5: Manifests are fetched at server startup and on demand via a manual refresh endpoint (FR-B9). v1 fetches manifests only at startup and on manual refresh (no automatic periodic refresh).
- FR-B6: Manifests are fetched via the GitHub raw content URL for the configured ref. No GitHub API token required for public repos.
- FR-B7: If a manifest fails to load, the loader logs a warning identifying the source and reason, then skips that source. Other sources continue to load. The startup never fails because of a remote source. On manual refresh, a failed source preserves its previous snapshot.
- FR-B8: The loader exposes a structured load-status summary (per source: loaded | skipped: <reason>, count of examples, last-fetched timestamp, resolved SHA) accessible via an actuator-style internal endpoint for diagnostics.
- FR-B9: **Manual refresh.** `POST /api/v1/examples/refresh` re-fetches every registered source's manifest and replaces the in-memory snapshot atomically on success. The endpoint returns the same per-source summary as FR-B8. v1 does not require authentication for the refresh endpoint.
- FR-B10: **Ref pinning.** At each load (startup or manual refresh), the loader resolves the configured `@ref` (or default branch) to a concrete **commit SHA** via the GitHub commits API. All manifest and example-content fetches in that load cycle use the resolved SHA, not the moving ref.

**FR Group C — Backend API**
- FR-C1: Extend the existing `GET /api/v1/metadata` response with an `examples` list with manifest fields plus computed fields: `sourceRepo`, `sourceRepoSha`, `sourceRepoUrl`, `isDownloadable`.
- FR-C2: Add `GET /api/v1/examples/{owner}/{repo}/{exampleId}/download` returning a ZIP of the example's subdirectory content. Route this endpoint separately from static resources to ensure proper error handling.
- FR-C3: The download endpoint resolves `{owner}/{repo}/{exampleId}` to its **pinned SHA** and serves a ZIP. Streaming used throughout. If the example subpath does not exist, the endpoint returns 404 with a structured error body.
- FR-C4: If the source repo or example id is unknown, the download endpoint returns `404` with a structured error body. If GitHub is unreachable on a cache miss, it returns `502`. A cache hit succeeds regardless of GitHub availability.
- FR-C5: **Example validation (v1).** On manifest load, the loader asynchronously validates each example by making a HEAD request. Validation is non-blocking. `isDownloadable` is initially `true` (optimistic); when validation completes, it is set to `true` or `false`.
- FR-C6: Manifest content is kept in memory after the most recent successful load. It is not persisted to disk.
- FR-C7: **ZIP cache.** Downloaded example ZIPs are cached on disk. Cache key includes the resolved SHA. Bounded by total size (default 512 MB) with LRU eviction.
- FR-C8: **Download telemetry.** The download endpoint increments an in-process counter per `(sourceRepo, exampleId)` and exposes the snapshot via `/actuator/examples`.

**FR Group D — Frontend Gallery UI**
- FR-D1: The existing `GalleryView.vue` renders the **"Examples"** subsection. The existing "Project Types" subsection remains unchanged. A short blurb introduces the Examples section.
- FR-D2: A new `ExampleGalleryCard.vue` renders each example with icon, title, shortDescription, tag chips, runtime/build-system badges, complexity badge, "Download" button, "View on GitHub" link, and a **question mark icon** (?) that toggles an expandable section. When expanded, the detail view shows longDescription, integrations, bpmnConcepts, requires, authors, license, lastUpdated, tag chips, and pinned commit SHA.
- FR-D3: A search box filters the Examples section by title, description, tags, and integrations.
- FR-D4: Filter chips let users narrow the Examples section by `runtime`, `buildSystem`, `complexity`, and `integrations`.
- FR-D5: When no examples load, the Examples section renders an empty state explaining how to register a repo and links to the docs.
- FR-D6: **Download button and error handling.** The "Download" button triggers a browser download. At card load, the example's existence is validated: if the example exists, the button is enabled; if not, the button is **disabled** and a small **warning icon** (⚠️) appears next to the button with a tooltip. Clicking the warning icon opens the source repository so the user can report the issue.
- FR-D7: Tag chips reuse the existing `Tag` model and `tagColors.ts` styling; new categories added to `TagCategory` if not present.

**FR Group E — Documentation**
- FR-E1: Add `docs/examples-repository-format.md` documenting manifest format, schema, examples, instructions.
- FR-E2: The Examples section in the gallery includes a link "Publish your own examples →" pointing to that doc.
- FR-E3: The main README links to `docs/examples-repository-format.md` under a "Contributing examples" section.
- FR-E4: The doc is the source of truth for the manifest schema.

### Non-Functional Requirements

- **NFR-1 Performance:** Total startup overhead ≤ 3s for N ≤ 10 sources on a normal network. Loader uses a hard per-source timeout of 5s.
- **NFR-2 Resilience:** Failure of any single source never blocks startup or breaks the gallery.
- **NFR-3 Security:** Only public GitHub HTTPS endpoints are contacted. No credentials sent. Manifest parsing uses SafeConstructor.
- **NFR-4 Size limits:** Manifests > 256 KB rejected. Per-example payload ≤ 50 MB. Streaming used throughout.
- **NFR-5 Compatibility:** `/api/v1/metadata` stays backwards-compatible — `examples` is an additive field.
- **NFR-6 Observability:** Per-source load result and per-download attempts logged at INFO with structured fields.
- **NFR-7 Forward compatibility:** Unknown manifest fields ignored; `apiVersion` checked on major only.

### Additional Requirements

None (PRD is self-contained).

### UX Design Requirements

None (PRD includes UI specs).

### FR Coverage Map

| FR | Epic | Context |
|---|---|---|
| FR-A1 through A7 | Epic 1 | Manifest schema definition |
| FR-B1 through B10 | Epic 1 | Repository config & loading with ref pinning |
| FR-C1 | Epic 2 | Metadata response for gallery |
| FR-C2, C3, C4 | Epic 3 | Download endpoint & error handling |
| FR-C5, C6 | Epic 1 | Async validation & memory storage |
| FR-C7, C8 | Epic 3 | ZIP caching & telemetry |
| FR-D1 | Epic 2 | Gallery UI section |
| FR-D2 | Epic 3 | Download button UI |
| FR-D3, D4, D5 | Epic 2 | Search, filters, empty state |
| FR-D6 | Epic 3 | Download error handling UI |
| FR-D7 | Epic 2 | Tag styling |
| FR-E1, E2, E3, E4 | Epic 4 | Documentation & links |
| NFR-1 through NFR-7 | All | Integrated throughout epics |

## Epic List

### Epic 1: Manifest Loading & Repository Registry

System can load examples from multiple configured GitHub repositories with intelligent validation and resilience.

**User Outcome:** Operators register repositories; system loads and validates examples automatically; failures in one source don't break others.

**FRs Covered:** FR-A1 through A7 (manifest schema), FR-B1 through B10 (repository loading, ref pinning, refresh), FR-C5, C6 (async validation, memory storage)

**Key Implementation Notes:**
- Manifest loader service runs at startup and on manual refresh
- All fetches use pinned commit SHA for consistency
- Async validation non-blocking; `isDownloadable` flag reflects status
- Graceful degradation: failed sources skip but don't break startup
- Spring properties + env var override for configuration

**Dependencies:** None (foundation epic)  
**Standalone:** Yes - enables all downstream features

---

### Epic 2: Example Gallery - Browse & Discover

Developers can find and explore examples with powerful search and filtering.

**User Outcome:** Users discover examples matching their needs (by runtime, build system, complexity, integration tags); expand cards to see details.

**FRs Covered:** FR-D1 (gallery section with blurb), FR-D3, D4, D5 (search box, filter chips, empty state), FR-C1 (metadata response with `isDownloadable`), FR-D7 (tag styling)

**Key Implementation Notes:**
- New `ExampleGalleryCard.vue` component with expanded detail view
- Search filters by title, description, tags, integrations
- Filter chips for runtime, buildSystem, complexity, integrations
- Question mark icon (?) toggles expanded detail view
- Empty state explains how to register repos and links to docs
- Reuses existing `Tag` model and `tagColors.ts`

**Dependencies:** Epic 1 (needs loaded manifests via metadata response)  
**Standalone:** Yes - complete browse/search workflow

---

### Epic 3: Example Download - One-Click ZIP

Users can download examples with graceful error handling.

**User Outcome:** One-click download of example as ZIP; clear feedback if example is broken or unavailable.

**FRs Covered:** FR-D2, D6 (card UI with Download button and error icon), FR-C2, C3, C4 (download endpoint, routing, error responses), FR-C7, C8 (ZIP caching, telemetry)

**Key Implementation Notes:**
- Download button enabled if `isDownloadable = true`, disabled with ⚠️ warning icon if false
- Warning icon links to source repo for user reporting
- Streaming throughout (GitHub tarball → ZIP filter → cache → client)
- LRU-bounded cache keyed by (owner, repo, sha, exampleId)
- 404 on missing example; 502 on unreachable GitHub
- In-process telemetry counter per (sourceRepo, exampleId)
- Endpoint routed separately from static resources to ensure proper error handling

**Dependencies:** Epic 1 (needs validation status, source repo URL)  
**Standalone:** Yes - complete download workflow

---

### Epic 4: Documentation for Contributors

Example authors understand the manifest format and can publish examples.

**User Outcome:** Contributors can publish examples to registered repositories following clear guidelines; users learn how to contribute.

**FRs Covered:** FR-E1 (manifest format docs with full schema, examples, instructions), FR-E2 (gallery link "Publish your own examples →"), FR-E3 (README integration), FR-E4 (docs as source of truth)

**Key Implementation Notes:**
- New `docs/examples-repository-format.md` documents manifest schema, publishing instructions, registry registration process
- Gallery links to published doc
- README gets "Contributing examples" section linking to same doc
- Doc covers forward-compatibility expectations for manifest versions

**Dependencies:** Epic 1 (documents the manifest schema)  
**Standalone:** Yes - enables community contribution

---

---

## Pre-Sprint Design Review (Prerequisite for Epic 2 & 3)

**Document:** `examples-gallery-ui-spec.md`  
**Purpose:** Formalize interaction affordances and error handling UI before devs code.

**Key Clarifications:**
1. **"?" Button Spec** — Position (top-right), state changes (rotate on toggle), keyboard interaction, tooltip
2. **⚠️ Warning Icon** — Placement, click behavior (opens repo), tooltip
3. **Download Progress UI** — Spinner animation, error toast format
4. **Mobile Layout** — Card adaptation to 320px screens (font sizes, button stacking)
5. **Error Response Format** — 404/502/413 JSON structure for backend
6. **Cache Eviction** — LRU policy, staleness handling
7. **Tag Categories** — New categories (runtime, integration, concept) + tagColors.ts updates

**Review Checklist (sign-off by frontend lead + UX before coding):**
- [ ] All button specs reviewed and approved
- [ ] Mobile layout breakpoints validated
- [ ] Error response format confirmed with backend
- [ ] Tag colors finalized
- [ ] Acceptance criteria understood

**Estimated Time:** 30-minute design review call

---

**Status:** Epic structure approved. Design spec created. Ready for pre-sprint review & story creation.

---

# Stories by Epic

## Epic 1: Manifest Loading & Repository Registry

### Story 1.1: Define Manifest Schema & Data Model

As a **backend engineer**,
I want **to define the Example entity and manifest schema**,
So that **all subsequent loader stories have a clear data contract**.

**Acceptance Criteria:**

**Given** the PRD specifies manifest structure (FR-A1 through A7)  
**When** I create the Example domain model and manifest parser  
**Then** the Example class includes all required fields: `id`, `title`, `shortDescription`, `path` (mandatory) plus all optional fields (longDescription, tags, buildSystem, runtime, icon, etc.)  
**And** unknown manifest fields are ignored without error (forward-compatibility)  
**And** `apiVersion: operaton-starter/v1` is parsed; manifests with unknown major versions log a warning but don't fail parsing  
**And** manifest YAML larger than 256 KB is rejected with a logged warning  

**Example model structure:**
```java
public record Example(
  String id,
  String title,
  String shortDescription,
  String path,
  String owner,
  String repo,
  String sourceRepoSha,
  // ... all optional fields
) {}
```

**Unit test:** Parse sample `.operaton-starter.yml` (from PRD appendix); verify all fields hydrate correctly; verify unknown fields don't throw exceptions.

---

### Story 1.2: Load Manifests from GitHub at Startup

As a **system operator**,
I want **manifests from registered repositories to be loaded automatically when the server starts**,
So that **examples are available immediately without manual intervention**.

**Acceptance Criteria:**

**Given** `starter.examples.repositories` Spring property configured (e.g., `starter.examples.repositories[0]=operaton/operaton-examples`)  
**When** the ExampleRepositoryLoader bean initializes at application startup  
**Then** for each registered repository, the loader fetches `.operaton-starter.yml` from GitHub raw content URL  
**And** fetches succeed within 5 seconds per source (hard timeout); sources exceeding timeout are skipped with a warning  
**And** total startup overhead is ≤ 3 seconds for N ≤ 10 sources (fetches run in parallel, not sequentially)  
**And** the in-memory ExampleRegistry snapshot is populated with all successfully loaded examples  
**And** if all sources fail to load, the registry is empty but startup is NOT blocked  

**Integration test:** Mock GitHub endpoint; verify parallel fetches; measure startup latency; verify in-memory snapshot populated.

---

### Story 1.3: Resolve Repository Refs to Commit SHAs (Ref Pinning)

As a **a developer downloading an example**,
I want **all example content fetched at a consistent commit SHA**,
So that **the downloaded ZIP matches the manifest metadata exactly (no mid-session repo changes)**.

**Acceptance Criteria:**

**Given** a configured repository ref like `operaton/operaton-examples@main` (or default branch if `@ref` omitted)  
**When** the loader initializes or manual refresh is triggered  
**Then** the loader calls GitHub commits API to resolve the ref to a concrete commit SHA  
**And** API call: `GET /repos/{owner}/{repo}/commits/{ref}` with `Accept: application/vnd.github.sha` header  
**And** the resolved SHA is stored in the ExampleRegistry and stamped onto each Example as `sourceRepoSha`  
**And** all subsequent manifest fetches and download operations in that load cycle use the pinned SHA, never the moving ref  
**And** if SHA resolution fails (404, network error), the source is skipped per FR-B7 (graceful degradation)  

**Unit test:** Mock commits API; verify SHA resolution; verify SHA is immutable across load cycle.

---

### Story 1.4: Asynchronously Validate Example Paths

As a **a frontend developer rendering a gallery card**,
I want **to know if an example's ZIP can actually be downloaded before the user clicks Download**,
So that **I can disable the button gracefully and show a warning icon instead of a 404 error**.

**Acceptance Criteria:**

**Given** manifests are loaded and examples are in the registry  
**When** the loader completes manifest load  
**Then** the loader asynchronously (non-blocking) validates each example by making a HEAD request to the GitHub tarball endpoint  
**And** validation URL: `https://codeload.github.com/{owner}/{repo}/tar.gz/{sourceRepoSha}` with path filter (to check if example subpath exists)  
**And** `isDownloadable` field is initially set to `true` (optimistic) for all examples  
**And** as validation completes in the background, `isDownloadable` is updated to `true` (path exists) or `false` (path missing/network error)  
**And** validation does NOT block manifest availability — examples appear in the gallery immediately even if validation is still running  
**And** validation results are included in the load-status summary (FR-B8)  

**Integration test:** Mock tarball endpoint; return 200 for some examples, 404 for others; verify `isDownloadable` flag updates asynchronously.

---

### Story 1.5: Implement Manual Refresh Endpoint

As a **a system operator**,
I want **to refresh examples without restarting the server**,
So that **new examples from a repository are available immediately when I update the manifest**.

**Acceptance Criteria:**

**Given** the application is running with loaded examples  
**When** a POST request is made to `/api/v1/examples/refresh`  
**Then** the endpoint triggers the same manifest + validation flow as startup (load, pin SHAs, validate asynchronously)  
**And** on success, the in-memory ExampleRegistry snapshot is replaced atomically  
**And** the endpoint returns the per-source load-status summary (same format as FR-B8 diagnostics)  
**And** if a source fails during refresh, the previous snapshot for that source is preserved (FR-B7)  
**And** the endpoint response includes timestamps showing when each source was last refreshed  

**HTTP Response (200):**
```json
{
  "status": "refresh_complete",
  "sources": [
    {
      "repo": "operaton/operaton-examples",
      "status": "loaded",
      "exampleCount": 12,
      "resolvedSha": "abc1234567890abcdef",
      "lastFetched": "2026-06-15T14:30:00Z"
    },
    {
      "repo": "acme/operaton-samples",
      "status": "skipped",
      "reason": "manifest_parse_error",
      "lastFetched": "2026-06-15T14:29:45Z"
    }
  ]
}
```

**Integration test:** Trigger refresh; modify mock manifest; verify new examples appear; trigger refresh again with failed source; verify previous snapshot preserved.

---

### Story 1.6: Expose Load-Status Summary via Diagnostics Endpoint

As a **a system operator troubleshooting a broken gallery**,
I want **to see which example sources loaded successfully and which failed**,
So that **I can diagnose connectivity or manifest issues without reading logs**.

**Acceptance Criteria:**

**Given** examples have been loaded at startup or via manual refresh  
**When** a request is made to the diagnostics endpoint (via Spring Boot Actuator)  
**Then** a `/actuator/examples` endpoint exposes a per-source summary including:
- `status`: `loaded` or `skipped: <reason>`
- `exampleCount`: number of examples from this source
- `lastFetched`: ISO timestamp of last successful/attempted load
- `resolvedSha`: the pinned commit SHA for this source (or null if load failed)  

**Example response:**
```json
{
  "timestamp": "2026-06-15T14:30:00Z",
  "sources": [
    {
      "repo": "operaton/operaton-examples",
      "status": "loaded",
      "exampleCount": 12,
      "resolvedSha": "abc1234567890abcdef",
      "lastFetched": "2026-06-15T14:30:00Z"
    },
    {
      "repo": "broken-repo/operaton-examples",
      "status": "skipped",
      "reason": "network_timeout",
      "resolvedSha": null,
      "lastFetched": "2026-06-15T14:29:00Z"
    }
  ]
}
```

**Unit test:** Mock multiple sources (success + failures); verify endpoint response includes all sources with correct statuses.

---

### Story 1.7: Handle Manifest Load Failures Gracefully

As a **a developer who depends on the gallery**,
I want **the app to start even if one example repository is broken**,
So that **I'm not blocked by a flaky external dependency**.

**Acceptance Criteria:**

**Given** configured repositories include at least one broken source (404, network timeout, parse error, invalid schema)  
**When** the ExampleRepositoryLoader initializes at startup  
**Then** failed sources are logged at WARN level with structured fields: `source`, `reason`, `exampleCount_before_failure`  
**And** failed sources do NOT prevent other sources from loading (failures are isolated)  
**And** the application continues to start normally (startup is never blocked by manifest load failures)  
**And** the ExampleRegistry remains in-memory with successfully loaded examples only  
**And** on manual refresh, if a source fails, the previous snapshot for that source is preserved (existing examples remain available)  
**And** logging includes enough detail for ops to diagnose the issue (e.g., "network_timeout", "manifest_parse_error", "unknown_apiVersion")  

**Log example:**
```
WARN o.o.d.s.examples.ExampleRepositoryLoader : Failed to load examples from source
  source=broken-repo/operaton-examples
  reason=manifest_parse_error
  error=Invalid YAML in .operaton-starter.yml: unexpected character
```

**Integration test:** Configure one valid source + one broken source; verify startup succeeds; verify valid examples load; verify failure is logged.

---

### Story 1.8: Extend `/api/v1/metadata` with Examples List

As a **a frontend developer building the gallery**,
I want **to fetch the full example list from `/api/v1/metadata` alongside project types**,
So that **I have a single API contract for all gallery content**.

**Acceptance Criteria:**

**Given** examples have been loaded into the ExampleRegistry  
**When** a request is made to `GET /api/v1/metadata`  
**Then** the response includes an `examples` array (in addition to existing `projectTypes`)  
**And** each example in the array includes: all manifest fields plus computed fields: `sourceRepo`, `sourceRepoSha`, `sourceRepoUrl`, `isDownloadable`  
**And** `sourceRepoUrl` is an HTML GitHub URL to the example folder at the pinned SHA (e.g., `https://github.com/operaton/operaton-examples/tree/abc1234567890abcdef/examples/kafka-saga`)  
**And** if no examples are loaded, the `examples` array is empty (never null)  
**And** existing clients that ignore the `examples` field continue to work unchanged (backward compatibility per NFR-5)  

**Example response:**
```json
{
  "projectTypes": [ /* existing array */ ],
  "examples": [
    {
      "id": "kafka-saga",
      "title": "Kafka Saga Pattern",
      "shortDescription": "Example showing saga pattern with Kafka",
      "path": "examples/kafka-saga",
      "tags": [{ "label": "kafka", "category": "INTEGRATION" }],
      "complexity": "intermediate",
      "sourceRepo": "operaton/operaton-examples",
      "sourceRepoSha": "abc1234567890abcdef",
      "sourceRepoUrl": "https://github.com/operaton/operaton-examples/tree/abc1234567890abcdef/examples/kafka-saga",
      "isDownloadable": true,
      // ... all other manifest fields
    }
  ]
}
```

**Integration test:** Load examples; call `/api/v1/metadata`; verify response structure; verify all fields present; verify backward compatibility.

---

**Epic 1 Summary:** 8 stories, ~2-3 weeks. All FRs (FR-A, FR-B, FR-C5, C6) covered. All NFRs integrated (resilience, security, performance, observability).

---

## Epic 2: Example Gallery - Browse & Discover

### Story 2.1: Polish "?" Button Styling per UI Spec

As a **a developer exploring examples**,
I want **to see a clear "?" icon in the card header that toggles the detail view**,
So that **I can quickly expand a card to see full details without scrolling**.

**Acceptance Criteria:**

**Given** ExampleGalleryCard.vue has `detailsOpen` ref and toggle logic  
**When** the card is rendered  
**Then** a `<button>` element appears in the card header (top-right corner, inline with title)  
**And** the button displays a question mark icon (Unicode `?` or SVG glyph)  
**And** button size is 24×24px with minimum 24px touch target  
**And** button styling matches UI spec:
  - Default: transparent bg, neutral-500 text, border transparent
  - Hover: primary-blue text + border, rounded border, bg with 5% opacity
  - Focus: 2px outline with 2px offset
  - Expanded: rotated 180° (via `transform: rotate(180deg)`)  

**And** the button has `aria-expanded="true|false"` attribute (updates with state)  
**And** the button has `aria-controls="details-{example-id}"` (links to detail panel ID)  
**And** the button has `title="Show details"` tooltip  

**And** keyboard interaction works:
  - Enter/Space toggles expanded state
  - Escape (when focus is in expanded details panel) collapses details  

**And** the button is only rendered if `hasDetails` is true (i.e., there's detail content to show)  

**Unit test:** Render card with details; click "?"; verify expanded state; verify rotation CSS applied; test keyboard nav.

---

### Story 2.2: Add ⚠️ Warning Icon to Download Action

As a **a developer encountering a broken example**,
I want **to see a warning icon when an example is missing or unavailable**,
So that **I can click it to report the issue to the maintainer**.

**Acceptance Criteria:**

**Given** DownloadAction.vue receives `downloadStatus` prop with `isAvailable` field  
**When** `isAvailable === false` (example missing at pinned SHA)  
**Then** the Download button is disabled and styled:
  - Background: neutral-300
  - Text: neutral-400
  - Cursor: not-allowed  

**And** a warning icon (⚠️ emoji or SVG triangle) appears to the right of the Download button  
**And** the warning icon is styled per UI spec:
  - Size: 20×20px
  - Color: amber-600
  - Interactive: clickable `<button>` or `<a>` element  
  - Hover: 10% amber background, amber border  
  - Focus: 2px amber outline with 2px offset  

**And** clicking the warning icon opens `example.sourceRepoUrl` in a new tab (`target="_blank" rel="noopener noreferrer"`)  
**And** the warning icon has `title="Example not found in source repo. Report issue →"` tooltip  

**And** when `isAvailable === true`, the warning icon is hidden (no extra space)  
**And** download button remains clickable and spinner shows during download  

**Integration test:** Mock isAvailable true/false; verify button disabled state; verify icon appears/disappears; verify click opens repo link.

---

### Story 2.3: Update Tag Categories & Styling

As a **a developer filtering by integration type**,
I want **tags to render with appropriate colors based on their category**,
So that **I can quickly scan and identify examples by technology (Kafka, REST, BPMN, etc.)**.

**Acceptance Criteria:**

**Given** new tag categories in the PRD: RUNTIME, BUILD_SYSTEM, COMPLEXITY, INTEGRATION, CONCEPT  
**When** ExampleGalleryCard.vue renders tags  
**Then** `tagColors.ts` is updated to support all categories:

```typescript
export function tagChipClasses(category: TagCategory): string {
  switch (category) {
    case 'BPMN_CONCEPT':
    case 'CONCEPT':
      return 'bg-blue-100 text-blue-800'
    case 'TECHNOLOGY':
    case 'INTEGRATION':
      return 'bg-amber-100 text-amber-800'
    case 'PLATFORM':
      return 'bg-green-100 text-green-800'
    case 'STANDARD':
      return 'bg-purple-100 text-purple-800'
    default:
      return 'bg-neutral-100 text-neutral-600'
  }
}

export function metadataBadgeClasses(category: TagCategory): string {
  if (['RUNTIME', 'BUILD_SYSTEM', 'COMPLEXITY'].includes(category)) {
    return 'bg-neutral-50 text-neutral-900 border border-neutral-200'
  }
  return 'bg-neutral-50 text-neutral-900 border border-neutral-200'
}
```

**And** on the card, tags render using the appropriate function:
  - RUNTIME, BUILD_SYSTEM, COMPLEXITY → `metadataBadgeClasses()` (monochrome badges)
  - BPMN_CONCEPT, CONCEPT, TECHNOLOGY, INTEGRATION, PLATFORM, STANDARD → `tagChipClasses()` (colored chips)  

**And** in the expanded detail view, tags are rendered the same way (colored chips for flavor tags, monochrome badges for metadata)  
**And** tag colors meet WCAG AA contrast ratio (4.5:1 for text on background)  

**Unit test:** Render card with mixed tag categories; verify correct colors applied; verify contrast ratios.

---

### Story 2.4: Integration Test — Search + Filter Flow

As a **a frontend developer ensuring search works**,
I want **to verify that typing in the search box and toggling filter chips correctly narrows the examples list**,
So that **users can reliably find examples by searching and filtering**.

**Acceptance Criteria:**

**Given** GalleryView.vue with multiple examples (≥5 with different runtime, complexity, integrations)  
**When** a user types "kafka" in the search box  
**Then** `useGalleryFilters` filters examples by title, description, tags, and integrations  
**And** only examples matching the search query are displayed  

**When** a user clicks a runtime filter chip (e.g., "spring-boot")  
**Then** examples are further filtered to those matching the selected runtime  
**And** other runtime chips show as toggleable (clicking another runtime replaces the previous filter)  

**When** a user clicks multiple filter chips (e.g., complexity "intermediate" + integration "kafka")  
**Then** examples are filtered by ALL active filters (AND logic)  
**And** `hasActiveFilters` flag indicates filters are active  

**When** a user clears filters (via "Clear" button)  
**Then** all filters reset and all examples are shown  
**And** the search query clears  

**Example flow:**
1. User sees 12 examples
2. User types "saga" → sees 2 examples (saga in title/desc)
3. User clicks complexity "intermediate" → sees 1 example (saga + intermediate)
4. User clicks integration "kafka" → still sees 1 example (kafka-saga, intermediate)
5. User clicks "Clear" → sees all 12 examples again

**Integration test:** Mock metadata with 5+ diverse examples; test search, single filter, multi-filter, clear; verify DOM updates.

---

### Story 2.5: Responsive Layout Test — 320px Constraint

As a **a mobile user browsing examples on an iPhone SE**,
I want **the card layout to adapt gracefully to a 320px viewport**,
So that **I can browse and interact with cards without horizontal scrolling or cramped text**.

**Acceptance Criteria:**

**Given** ExampleGalleryCard.vue and GalleryView.vue with responsive CSS  
**When** rendered at 320px viewport width  
**Then** card layout adapts:
  - Icon size: 40×40px (vs. 48×48px at desktop)
  - Card title: font-size 0.875rem (vs. 1rem), may wrap to 2 lines
  - Short description: font-size 0.75rem, full width, may wrap
  - Tags: flex-wrap wrap, gap 0.5rem
  - Metadata badges: font-size 0.625rem  

**And** action row (Download + View on GitHub) adapts:
  - Option A (preferred): Stack vertically with full width (100% each)
  - Option B: Shrink font to 0.875rem, keep inline
  - Recommendation: Choose Option A for less cramped feel  

**And** grid layout adapts:
  - 320px: 1 column (single card per row)
  - 375px+: Stay 1 column until 640px breakpoint  
  - 640px+: 2 columns  
  - 768px+: 3 columns  

**And** no text truncation surprises (titles wrap naturally, not cut off)  
**And** no horizontal scroll on the card or page  
**And** touch targets remain ≥24px (buttons, icons)  
**And** the "?" button and ⚠️ icon are still clickable and clearly visible  

**Visual regression test:** Render card at 320px, 375px, 640px, 768px; screenshot compare; verify no overflow.

---

### Story 2.6: E2E Test — Full Gallery Workflow

As a **a product owner validating the gallery feature**,
I want **to verify the complete user workflow: browse → search → filter → expand → view details → download**,
So that **the gallery is production-ready and users can discover and access examples seamlessly**.

**Acceptance Criteria:**

**Given** the app is running with loaded examples (from Epic 1)  
**When** a user navigates to the Gallery page  
**Then** the Examples section is visible with:
  - Section title "Examples" and blurb ("Real-world runnable examples...")
  - GallerySearchBar with search input + runtime/buildSystem/complexity/integrations filters
  - Grid of ExampleGalleryCard components (responsive: 1/2/3 columns per breakpoint)
  - ExamplesEmptyState hidden (examples exist)  

**When** the user types "kafka" in search  
**Then** the list filters to examples matching "kafka" (in title, description, tags, or integrations)  

**When** the user clicks complexity filter "intermediate"  
**Then** examples are further filtered (kafka + intermediate)  

**When** the user clicks a "?" button on a card  
**Then** the detail panel expands, showing:
  - Long description (rendered markdown)
  - BPMN concepts (as tags)
  - Integrations (as tags)
  - Authors (as links if URLs provided)
  - License
  - Last updated date
  - Pinned commit SHA (7-char, monospace font)
  - Tags (same chips as card summary)  

**And** the "?" button rotates 180° to indicate expanded state  

**When** the user clicks the "Download" button  
**Then** the button shows spinner + "Downloading..." text  
**And** the browser downloads a ZIP file (filename: `{exampleId}.zip`)  

**When** the download completes  
**Then** the button returns to normal state ("Download")  
**And** the file is saved to the user's downloads folder  

**When** the user encounters a broken example (isDownloadable: false)  
**Then** the Download button is disabled (neutral-300 bg)  
**And** a ⚠️ warning icon appears with tooltip "Example not found. Report issue →"  
**And** clicking the icon opens the GitHub repo in a new tab  

**When** the user clears filters  
**Then** all examples are shown again  
**And** the search box is cleared  

**E2E test (Playwright/Cypress):** 
1. Navigate to gallery
2. Search for "kafka" → verify 2 examples shown
3. Click complexity filter → verify 1 example shown
4. Click "?" → verify details expand
5. Click Download → verify file downloads
6. Clear filters → verify all examples show again

---

**Epic 2 Summary:** 6 stories, ~1-2 weeks. All FRs (D1, D3-D5, D7, C1) covered. Frontend polish + integration testing.

---

## Epic 3: Example Download - One-Click ZIP

### Story 3.1: Implement Download Endpoint with Streaming

As a **a developer downloading an example**,
I want **to click Download and receive a ZIP file streamed from the server**,
So that **I can extract the example code and start working immediately**.

**Acceptance Criteria:**

**Given** an example exists in a registered repository  
**When** a request is made to `GET /api/v1/examples/{owner}/{repo}/{exampleId}/download`  
**Then** the endpoint:
  - Resolves `{owner}/{repo}/{exampleId}` to the pinned SHA from the ExampleRegistry
  - Fetches the GitHub tarball: `https://codeload.github.com/{owner}/{repo}/tar.gz/{sha}`
  - Filters the tarball to the example's subpath (e.g., `examples/kafka-saga/`)
  - Re-packs filtered content as a ZIP file
  - Streams the ZIP to the client  
  - Sets response headers: `Content-Type: application/zip`, `Content-Disposition: attachment; filename="{exampleId}.zip"`  

**And** the endpoint is routed separately from static resources (ensures proper error handling, not caught by static handler)  
**And** streaming is used throughout (no large in-memory buffering)  

**When** the download completes successfully  
**Then** a 200 OK response is returned with the ZIP file  

**When** the example path does not exist at the pinned SHA  
**Then** the endpoint returns 404 with structured error body:
```json
{
  "status": "not_found",
  "error": "Example path does not exist in repository",
  "details": {
    "sourceRepo": "operaton/operaton-examples",
    "sourceSha": "abc1234567890abcdef",
    "exampleId": "kafka-saga",
    "expectedPath": "examples/kafka-saga",
    "sourceRepoUrl": "https://github.com/operaton/operaton-examples/tree/abc1234567890abcdef/examples/kafka-saga"
  }
}
```

**When** GitHub is unreachable (network timeout, 5xx error)  
**Then** the endpoint returns 502 Bad Gateway with error body  

**When** the example ZIP exceeds 50 MB uncompressed  
**Then** the endpoint aborts, deletes the partial cache file, and returns 413 Payload Too Large with error body  

**Integration test:** Mock GitHub tarball; test success path (200), missing path (404), oversized ZIP (413), network error (502).

---

### Story 3.2: Implement ZIP Cache with LRU Eviction

As a **a system operator managing server disk usage**,
I want **downloaded ZIPs to be cached and reused on subsequent requests**,
So that **repeated downloads of the same example don't re-fetch from GitHub and stay within disk limits**.

**Acceptance Criteria:**

**Given** the first download request for an example  
**When** the download endpoint builds a ZIP and caches it  
**Then** the ZIP is written to disk at: `{java.io.tmpdir}/operaton-starter/examples-cache/{owner}/{repo}/{sha}/{exampleId}.zip`  
**And** the path is configurable via `starter.examples.cache.dir` property  

**When** a subsequent request for the same `(owner, repo, sha, exampleId)` is made  
**Then** the cached ZIP is served directly from disk (no GitHub fetch, no re-packing)  
**And** the cached ZIP is streamed to the client (not loaded fully into memory)  

**When** the total cache size exceeds `starter.examples.cache.maxSizeMb` (default 512 MB)  
**Then** LRU eviction removes the least recently accessed entries until cache size is below the limit  
**And** access time is tracked per ZIP file (time of last read)  

**When** the pinned SHA changes (e.g., via manual refresh to a new ref)  
**Then** old SHA entries in the cache are naturally evicted over time via LRU  
**And** new examples at the new SHA use a new cache key, so no conflicts  

**When** a download fails (404, 502, 413)  
**Then** NO partial cache file is left behind (any written file is deleted)  

**Cache invariant:**
- Cache keys are immutable: `(owner, repo, sha, exampleId)` uniquely identifies a ZIP
- SHAs are pinned, so cached ZIPs never go stale
- LRU eviction handles size bounds without manual intervention

**Unit test:** Mock cache directory; test cache hit, cache miss, eviction, stale entry cleanup.

---

### Story 3.3: Implement Download Telemetry Endpoint

As a **a product analyst tracking example adoption**,
I want **to see how many times each example has been downloaded**,
So that **I can identify which examples are most valuable to the community**.

**Acceptance Criteria:**

**Given** users download examples via the download endpoint  
**When** each download completes (success only, not failures)  
**Then** the endpoint increments an in-process counter per `(sourceRepo, exampleId)`  
**And** counter format: `Map<String, Map<String, Long>>` → `{sourceRepo: {exampleId: count, ...}, ...}`  
**And** counters are NOT persisted to disk (reset on server restart)  

**When** a request is made to `GET /actuator/examples` (via Spring Boot Actuator)  
**Then** the endpoint returns a snapshot of current download counters:
```json
{
  "timestamp": "2026-06-15T14:30:00Z",
  "downloads": {
    "operaton/operaton-examples": {
      "kafka-saga": 42,
      "dmn-rules": 15,
      "rest-service": 8
    },
    "acme/operaton-samples": {
      "payment-approval": 3
    }
  }
}
```

**And** the endpoint logs no PII (just counts)  
**And** no remote reporting (counts stay server-side only)  

**Unit test:** Call download endpoint multiple times; call `/actuator/examples`; verify counters incremented correctly.

---

### Story 3.4: Wire DownloadAction to Backend Download Endpoint

As a **a frontend developer integrating the download button**,
I want **the Download button to call the backend endpoint and handle the response**,
So that **users can seamlessly download examples without seeing network details**.

**Acceptance Criteria:**

**Given** DownloadAction.vue receives `example` prop and `downloadStatus` prop  
**When** the user clicks the Download button  
**Then** `useExampleDownload` composable calls `GET /api/v1/examples/{owner}/{repo}/{exampleId}/download`  
**And** sets `downloadStatus.isLoading = true` (triggers spinner)  
**And** while loading, the button shows "Downloading..." + spinner icon  

**When** the response succeeds (200 OK)  
**Then** the browser's native download behavior triggers (blob download)  
**And** `downloadStatus.isLoading = false`  
**And** the button returns to "Download" state  
**And** a success toast shows: "Downloaded {exampleId}.zip" (auto-dismiss in 3 seconds)  

**When** the response fails (404, 502, 413 or network timeout)  
**Then** `downloadStatus.error = error message` (one of: "Example not found", "GitHub unavailable", "Example too large", "Network error")  
**And** `downloadStatus.isLoading = false`  
**And** the button returns to "Download" state (clickable for retry)  
**And** an error toast shows with Retry button (see Story 3.5)  

**When** the user clicks the Retry button in the error toast  
**Then** the download is attempted again  

**Unit test (Vitest/Jsdom):** Mock fetch; test success (200), 404, 502, timeout; verify state updates; verify Retry works.

---

### Story 3.5: Implement Download Progress UI & Error Toast

As a **a developer experiencing a download failure**,
I want **to see a clear error message and have the option to retry**,
So that **I can recover from transient network issues without giving up**.

**Acceptance Criteria:**

**Given** a download fails (404, 502, 413, timeout)  
**When** the error is received  
**Then** an error toast appears at bottom-left of viewport with:
  - Error message (per UI spec): "Example not found" / "GitHub unavailable" / "Example too large (>50 MB)" / "Download timed out"
  - Dismiss button (X or "Dismiss")
  - Retry button  

**And** the toast styling per UI spec:
  - Background: rgb(239, 68, 68) (red-600)
  - Text: white
  - Position: bottom-left (mobile-friendly; not bottom-right which hides on small screens)
  - Duration: 6 seconds auto-dismiss OR manual dismiss  

**When** the user clicks Retry  
**Then** the same download is attempted again (reuses the same example/endpoint)  
**And** the loading spinner shows again  
**And** the error toast is replaced  

**When** the download succeeds on retry  
**Then** the file downloads and success toast shows (as per Story 3.4)  

**When** the download fails again on retry  
**Then** the error toast reappears (same format)  

**Toast messages (exact per FR-D6):**
- 404: "Example not found in source repo. Report issue."
- 502: "GitHub unreachable. Try again?"
- 413: "Example is too large to download (exceeds 50 MB)."
- Timeout: "Download took too long. Try again?"

**Component integration:** Toast component lives in GalleryView or global layout; useExampleDownload emits error events; toast listens and displays.

**Visual regression test:** Trigger each error type; screenshot toast styling; verify position, colors, buttons.

---

### Story 3.6: Integration Test — Download Endpoint & Cache

As a **a backend developer validating the download pipeline**,
I want **to verify that downloads work end-to-end: fetch tarball, pack ZIP, cache, serve**,
So that **the feature is production-ready and resilient to edge cases**.

**Acceptance Criteria:**

**Given** examples are loaded (from Epic 1) and the download endpoint is implemented  
**When** a download request is made for an example  
**Then** the full pipeline works:
  1. Endpoint resolves example to pinned SHA
  2. Fetches GitHub tarball: `https://codeload.github.com/{owner}/{repo}/tar.gz/{sha}`
  3. Filters tarball to example subpath
  4. Re-packs as ZIP
  5. Writes to cache: `{cache-dir}/{owner}/{repo}/{sha}/{exampleId}.zip`
  6. Streams ZIP to client
  7. Returns 200 OK + ZIP file

**When** a second request for the SAME `(owner, repo, sha, exampleId)` is made  
**Then** the cached ZIP is served directly (no GitHub fetch)  
**And** access time is updated in LRU tracker  

**When** a request is made for a DIFFERENT example  
**Then** a new ZIP is cached separately  

**When** the cache exceeds max size  
**Then** least recently accessed ZIPs are evicted  

**When** the example path does not exist  
**Then** a 404 is returned with structured error body  

**When** the ZIP would exceed 50 MB  
**Then** a 413 is returned; partial cache file is deleted  

**When** GitHub is unreachable  
**Then** a 502 is returned  

**Integration test (TestRestTemplate + WireMock):**
1. Mock GitHub tarball endpoint
2. Call download → verify 200, file received, cache written
3. Call download again → verify cache hit (no new GitHub call)
4. Trigger 404 scenario → verify 404 response + no cache
5. Trigger 413 scenario → verify 413 response + cache cleaned up
6. Trigger GitHub down → verify 502 response

---

### Story 3.7: E2E Test — Full Download Workflow

As a **a product owner verifying the download feature**,
I want **to verify the complete download workflow from button click to file save**,
So that **users can reliably download and extract examples**.

**Acceptance Criteria:**

**Given** the app is running with examples loaded and the download endpoint working  
**When** a user navigates to an example card  
**Then** the Download button is visible (enabled or disabled based on `isDownloadable`)  

**When** `isDownloadable === true` (example exists)  
**Then** the Download button is enabled and clickable  

**When** the user clicks Download  
**Then** the button shows "Downloading..." + spinner  
**And** the API call is made to the download endpoint  

**When** the download completes successfully  
**Then** the browser's native download dialog appears  
**And** the file `{exampleId}.zip` is saved to the user's Downloads folder  
**And** a success toast shows: "Downloaded {exampleId}.zip" (optional, auto-dismisses)  
**And** the button returns to "Download" state  

**When** the download fails (e.g., GitHub unreachable)  
**Then** an error toast shows with Retry button  
**And** clicking Retry attempts the download again  

**When** `isDownloadable === false` (example missing at pinned SHA)  
**Then** the Download button is disabled (neutral-300 bg, neutral-400 text)  
**And** a ⚠️ warning icon appears with tooltip "Example not found. Report issue →"  
**And** clicking the icon opens the GitHub repo in a new tab  

**Full flow test (Playwright):**
1. Load gallery with mixed examples (available + unavailable)
2. Click Download on available example → verify file downloads
3. Click Download on unavailable example → verify button disabled, ⚠️ icon present
4. Trigger network error (mock GitHub down) → verify error toast + Retry button
5. Click Retry → verify re-attempt works
6. Verify downloaded file can be extracted (optional, higher-level validation)

---

**Epic 3 Summary:** 7 stories, ~1.5-2 weeks. All FRs (D2, D6, C2-C4, C7-C8) covered. Backend download pipeline + frontend integration + comprehensive testing.

---

## Epic 4: Documentation for Contributors

### Story 4.1: Create Examples Repository Format Documentation

As a **content author**,
I want **clear documentation on how to write a `.operaton-starter.yml` manifest and publish examples**,
So that **I can contribute examples to the registry without guessing the format**.

**Acceptance Criteria:**

**Given** the PRD specifies manifest schema (FR-A1 through A7)  
**When** I create `docs/examples-repository-format.md` (or similar) in the operaton-starter project  
**Then** the documentation includes:

1. **Overview section** (100 words max): What manifests are, why they matter, how they make examples discoverable

2. **Schema reference** with:
   - `apiVersion` — must be `operaton-starter/v1`
   - Required fields: `id`, `title`, `shortDescription`, `path` with descriptions and constraints
   - Optional fields (grouped by category):
     - **Descriptive:** longDescription, tags, integrations, bpmnConcepts, authors, license, documentationUrl, demoVideoUrl
     - **Technical:** buildSystem, runtime, requires, operatonVersion, javaVersion, complexity
     - **Media:** icon (emoji or URL), screenshots
     - **Metadata:** lastUpdated (ISO 8601)
   - Field-level constraints: max lengths, allowed enums (buildSystem, runtime, complexity), URL format

3. **Manifest example** with realistic values and inline comments:
   ```yaml
   apiVersion: operaton-starter/v1
   repository:
     name: Example Repository
     maintainer: Jane Doe
   examples:
     - id: kafka-saga
       title: Kafka-based Saga Pattern
       shortDescription: Demonstrates distributed saga pattern using Kafka events
       path: examples/kafka-saga
       # ... optional fields
   ```

4. **Repository layout** guide showing the expected directory structure:
   ```
   operaton-examples/
   ├── .operaton-starter.yml    # Manifest at root
   ├── examples/
   │   ├── kafka-saga/          # Example subdirectory
   │   │   ├── pom.xml          # Example's own build files
   │   │   └── src/
   │   └── saga-with-jdbc/      # Another example
   ```

5. **How to register** — brief guidance on contacting maintainers to register a repo in `starter.examples.repositories`

6. **Forward-compatibility** section explaining that unknown fields are ignored; authors should not worry about future schema versions

7. **Validation checklist:**
   - [ ] Each example has a unique `id`
   - [ ] `path` points to existing subdirectory
   - [ ] `shortDescription` is ≤ 200 characters
   - [ ] manifest ≤ 256 KB (if defined)
   - [ ] YAML is valid (use a linter)

**Location:** `docs/examples-repository-format.md` (discoverable from README)  
**Word count:** 600–800 words (concise, scannable)  
**Review criteria:** A new contributor should understand the schema in under 10 minutes

**Unit test:** None required (documentation task)

---

### Story 4.2: Add "Publish Your Own Examples" Link to Gallery UI

As a **user browsing the gallery**,
I want **an obvious way to learn how to contribute my own examples**,
So that **I'm encouraged to share examples without searching the docs**.

**Acceptance Criteria:**

**Given** the gallery view displays examples  
**When** I scroll to the empty state OR the bottom of the gallery  
**Then** I see a call-to-action card or link section with:

**Card text (Recommended):**
```
Publishing Your Own Examples

Have an Operaton pattern or workflow you'd like to share?

[Learn how to contribute →]
```

**OR inline link (if less intrusive):**
```
Can't find what you need? Learn how to publish your own examples →
```

**Behavior:**
- Link href: `/docs/examples-repository-format.md` (or external URL if published online)
- Text color: primary-blue (consistent with other gallery links)
- Hover state: underline + blue highlight
- Mobile (320px): Text is full-width, link is tap-friendly (≥44px height)

**Styling:**
- If card: neutral-50 bg with border, 1rem padding, centered text, 8–12px gap between text and button
- If inline: inherit parent typography, no special styling

**Placement:** 
- Below the gallery grid OR at the bottom of the empty state message (whichever is rendered)
- No overlap with other UI elements

**A/B criteria:** Track clicks via telemetry (optional). Success: ≥1 user navigates to contributing docs per week (baseline after launch).

**Implementation note:** Vue component ExampleGalleryView or empty-state sub-component

---

### Story 4.3: Update README with Contributing Examples Section

As a **new contributor exploring operaton-starter**,
I want **the main README to mention examples and how to contribute them**,
So that **I discover the feature and contributing pathway early**.

**Acceptance Criteria:**

**Given** the operaton-starter README already documents the project  
**When** I read the README  
**Then** I find a new section titled **"Contributing Examples"** or **"Share Your Examples"** containing:

1. **Brief intro** (1–2 sentences): "The Examples Gallery showcases real-world Operaton patterns. You can contribute your own examples by publishing a repository with a manifest file."

2. **Quick start link** (inline or button): "See [the examples repository format guide](./docs/examples-repository-format.md)" or URL to published docs

3. **Example structure callout** (3–4 lines):
   ```
   All you need:
   - A GitHub repository with example code
   - A `.operaton-starter.yml` manifest at the root
   - Let us know the repo URL
   ```

4. **Contact/Process** (1–2 lines): "To register your repository, [open an issue](https://github.com/operaton/operaton-starter/issues) with the repo URL and a brief description, or contact us at [email/Slack channel]."

**Placement:**
- After "Getting Started" or "Features" section
- Before "Development" or "Contributing Code" (if those sections exist)
- Keep README scannable: max 100 words for this section

**Cross-link confirmation:**
- README links to `docs/examples-repository-format.md` ✓
- `docs/examples-repository-format.md` mentions the README (optional back-link) ✓

**Review:** README edits pass project style guide; links are valid; tone matches existing README

---

### Story 4.4: Publish Examples Documentation Online

As a **community contributor looking for publishing guidelines**,
I want **accessible, discoverable documentation outside the GitHub repo (e.g., website, documentation site)**,
So that **guidelines are easy to find and share with others**.

**Acceptance Criteria:**

**Given** the `docs/examples-repository-format.md` is complete  
**When** the documentation is published  
**Then** it is available at a stable, public URL (e.g., `https://docs.operaton.io/examples/repository-format` or similar)

**Publishing method** (choose the project's standard):
- Option A: Commit to documentation source repo (e.g., operaton-docs) with build pipeline
- Option B: Publish via wiki or static site generator
- Option C: Host inline in operaton-starter and expose via project docs

**Requirements:**
- URL is stable and doesn't change (redirect if refactored)
- Content is searchable (on-site search or indexed by search engines)
- Page includes breadcrumb or site navigation for discoverability
- Metadata (title, description) is set for SEO
- Mobile-friendly rendering (responsive, readable on 320px screens)
- Last-updated date is visible

**Cross-link checklist:**
- Gallery UI link points to published URL ✓
- README link points to published URL ✓
- operaton-starter repo README or main site links to the guide ✓

**Verification:** 
- URL is live and returns 200 OK
- Page renders correctly on desktop + mobile
- Search engines can find it (basic SEO check)

**Success metric:** ≥1 external contributor uses the guide to publish an example within 6 months of launch

---

**Epic 4 Summary:** 4 stories, ~1–1.5 weeks. Documentation is low-complexity but high-value. Covers all FRs (E1–E4). Enables community contribution and makes Examples Gallery discoverable as a contribution pathway.

---

## Final Validation Checklist

**FR Coverage by Epic:**
- Epic 1: FR-A1 through A7, FR-B1 through B10, FR-C1, FR-C5, FR-C6, FR-C8
- Epic 2: FR-D1, FR-D3, FR-D4, FR-D5, FR-D6
- Epic 3: FR-C2 through C4, FR-C7, FR-D2, FR-D6 (UI polish)
- Epic 4: FR-E1 through E4

**NFR Coverage:**
- NFR-1 (Performance): Epic 1 (startup ≤3s, FR-B5), Epic 3 (streaming, no large buffers)
- NFR-2 (Scalability): Epic 1 (parallel fetches), Epic 3 (LRU cache, bounded size)
- NFR-3 (Security): Epic 1 (SafeConstructor for YAML)
- NFR-4 (Privacy/Compliance): Epic 3 (no PII in telemetry)

**All 36 FRs distributed:** ✓  
**All 7 NFRs addressed:** ✓  
**User value per epic clear:** ✓  
**Dependencies manageable:** ✓  
**Feasibility for 2–3 week sprints:** ✓

---

**Status:** Epic and story breakdown complete. Ready for team handoff and sprint planning.
