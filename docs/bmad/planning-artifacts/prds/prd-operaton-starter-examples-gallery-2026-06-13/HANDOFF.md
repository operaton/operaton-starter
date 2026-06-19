# Examples Gallery v1 — Team Handoff

**PRD Status:** Final (2026-06-15)  
**Decision Log:** `.decision-log.md` (full audit trail of all decisions)  
**Implementation Notes:** `addendum.md` (file-level details, schema reference)

---

## For Architecture Team

**Key decisions affecting design:**

- **Ref pinning to commit SHA** (FR-B10): All manifest + download fetches use pinned SHA, not moving ref. Isolates user view from mid-session repo changes.
- **Async validation** (FR-C5): Example paths validated non-blocking on load; `isDownloadable` flag reflects existence. Keeps startup fast.
- **Server-side ZIP cache** (FR-C7): LRU-bounded cache keyed by `(owner, repo, sha, exampleId)`. SHA pinning gives natural invalidation on refresh.
- **Streaming throughout** (FR-C3, NFR-4): No large in-memory buffering; streams GitHub tarball, re-packs ZIP, streams to cache.
- **No GitHub API token required** (FR-B6): Public repos only; public raw content + codeload endpoints.
- **Resilience by isolation** (FR-B7): Single source failure never blocks startup or gallery. Failed sources preserve previous snapshot on manual refresh.

**Open design questions:**
- Rate-limiting mitigation (R-1): Consider caching validation results to avoid re-validating on every startup.
- Spring Boot Actuator assumption (FR-B8): Ensure already on classpath or add dependency.

**Start with:** `prd.md` section 5 (FR Groups A–C) + `addendum.md` for implementation pointers.

---

## For UX/Design Team

**Core interactions:**

- **Card-level UI** (FR-D2): Icon, title, short description, tag chips, badges (runtime/build/complexity), "Download" button, "View on GitHub" link, **`?` icon to expand details.**
- **Expanded detail view** (FR-D2): Long description, integrations, concepts, requirements, authors, license, last updated, tags (same chips), commit SHA footer.
- **Error handling** (FR-D6): Missing examples get **disabled Download button + ⚠️ warning icon** (links to repo for reporting). No error page.
- **Gallery controls** (FR-D3, FR-D4): Search (title, description, tags, integrations) + filter chips (runtime, buildSystem, complexity, integrations).
- **Empty state** (FR-D5): Explain how to register a repo + link to docs.

**Design notes:**
- Tag chips reuse existing `Tag` model + `tagColors.ts`; add categories if needed (runtime, integration, concept).
- Icon behavior (FR-A4): Emoji or SVG/PNG ≤ 64×64; falls back to generic.
- Button label: "Download" (not "Download ZIP").

**Start with:** `prd.md` section 5 (FR Group D) for full UI spec. `prd.md` section 4 (User Journeys) for context.

---

## For Implementation Team

**Backend priorities:**

1. **Loader service** (FR Group B): Fetch manifests from repo list at startup + manual refresh. Resolve ref → SHA. Handle failures gracefully (log + skip).
2. **Download endpoint** (FR Group C): `GET /api/v1/examples/{owner}/{repo}/{exampleId}/download` → ZIP. Route *separately from static resources* (key to avoiding 500 errors). Cache server-side.
3. **Validation** (FR-C5): Async HEAD requests to validate example paths on load. Non-blocking; `isDownloadable` flag reflects result.
4. **Extend metadata** (FR-C1): Add `examples` list to `GET /api/v1/metadata` response with computed fields (`isDownloadable`, `sourceRepoUrl`, etc.).
5. **Frontend** (FR Group D): New `ExampleGalleryCard.vue` component; integrate into `GalleryView.vue`. Add `?` expand icon + warning icon handling.

**Critical details:**
- **Streaming required** (NFR-4): GitHub tarball → ZIP filtering → cache → client download. No large buffers.
- **Error routing** (FR-C3, FR-C4): Download endpoint must return 404 (not 500) if example missing. Ensure route precedence so `/api/v1/examples/*` is matched before static patterns.
- **Size limits** (NFR-4): Manifest ≤ 256 KB; per-example ZIP ≤ 50 MB (configurable).
- **YAML parsing** (NFR-3): Use SafeConstructor; reject manifests with arbitrary class instantiation.
- **Telemetry** (FR-C8): In-process counter per `(sourceRepo, exampleId)` via `/actuator/examples`.

**Configuration:**
- Spring property: `starter.examples.repositories[]` (overridable by `STARTER_EXAMPLES_REPOSITORIES` env var).
- Default: `operaton/operaton-examples`.
- Cache path: `${java.io.tmpdir}/operaton-starter/examples-cache/{owner}/{repo}/{sha}/{exampleId}.zip` (overridable).
- Cache size: 512 MB default (configurable via `starter.examples.cache.maxSizeMb`).

**Start with:**
- `prd.md` section 5 (FR Groups A–C for backend, D for frontend) + `addendum.md` for file-level notes.
- `prd.md` section 6 (NFR) for quality constraints.
- `.decision-log.md` for rationale on key choices (ref pinning, async validation, error handling strategy).

---

## Coordination

- **Architecture ↔ Implementation:** Design loader concurrency (parallel manifest fetches), streaming pipeline, cache strategy before coding.
- **UX ↔ Frontend:** Finalize card layout, expand/collapse affordance, error icon tooltip copy before component build.
- **All teams:** Refer to `.decision-log.md` if rationale for a requirement is unclear.

---

## Non-Goals (v1)
- User-configurable repo list in UI (config only, no admin UI)
- Private repo auth (public only)
- Server-side example validation/testing
- Per-example version pinning

See PRD section 2 for full non-goals list.

---

**Questions?** Refer to PRD section 8 (Risks & Open Questions) or `.decision-log.md` for context on deferred decisions (e.g., refresh endpoint auth).
