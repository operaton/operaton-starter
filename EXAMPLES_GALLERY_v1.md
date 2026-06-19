# Examples Gallery v1 — PRD & Implementation Guide

**Status:** Final PRD (2026-06-15)  
**Scope:** v1 adds remote example repositories with searchable gallery, ZIP download, and async validation.

## Quick Links

| Artifact | Purpose | Audience |
|----------|---------|----------|
| [Full PRD](docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/prd.md) | Complete specification (2. Goals, 5. Functional/Non-Functional Requirements, 7. Success Metrics) | All teams |
| [Team Handoff](docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/HANDOFF.md) | Curated summaries by team (Architecture, UX, Implementation) | Implementation kick-off |
| [Decision Log](docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/.decision-log.md) | Rationale for all key decisions + recent updates (UI refinements, error handling) | Design review, architecture validation |
| [Addendum](docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/addendum.md) | Implementation notes, file-level pointers, manifest schema reference | Backend implementation |

## Key Features v1

✅ **Manifest schema** — External repos publish `.operaton-starter.yml` at root  
✅ **Maintainer-controlled registry** — Spring property + env var override  
✅ **Ref pinning** — All fetches use pinned commit SHA (consistent snapshots)  
✅ **Async validation** — Examples validated non-blocking; `isDownloadable` flag reflects status  
✅ **Server-side ZIP cache** — LRU-bounded, SHA-keyed (auto-invalidates on refresh)  
✅ **Searchable/filterable gallery** — Search + filter chips (runtime, buildSystem, complexity, integrations)  
✅ **Graceful error handling** — Missing examples disable Download button + warning icon with repo link  
✅ **Documentation** — Manifest format guide + contributing instructions  

## Critical Design Decisions

1. **Ref pinning (FR-B10):** All manifest + download fetches use pinned commit SHA, not moving ref. Isolates user view from mid-session repo changes.
2. **Async validation (FR-C5):** Example paths validated non-blocking on load. Keeps startup fast.
3. **Error handling (FR-D6, FR-C3):** Missing examples → disabled button + warning icon (not error page or 500). Users can report via repo link.
4. **Streaming throughout (FR-C3):** GitHub tarball → ZIP filter → cache → client. No large buffers.
5. **Failure isolation (FR-B7):** Single source failure never blocks startup. Failed sources preserve previous snapshot.

See `.decision-log.md` for full rationale on each decision.

## Implementation Sequence

**Phase 1 — Backend (Weeks 1–2)**
- Manifest loader + SHA resolution (FR-B1 through FR-B10)
- Download endpoint + ZIP caching (FR-C2, FR-C3, FR-C7)
- Async validation (FR-C5)
- Extend metadata response (FR-C1)

**Phase 2 — Frontend (Weeks 2–3)**
- `ExampleGalleryCard.vue` component (FR-D2)
- Gallery search + filter (FR-D3, FR-D4)
- Download error handling UI (FR-D6, warning icon)
- Empty state (FR-D5)

**Phase 3 — Documentation (Week 3)**
- Manifest format guide (FR-E1)
- Update README (FR-E3)

## Non-Goals v1
- User-configurable repo list in UI (config-only)
- Private repo authentication
- Server-side example validation/testing
- Per-example version pinning

See PRD section 2 for details.

## Configuration

**Spring property:**
```properties
starter.examples.repositories[0]=operaton/operaton-examples
starter.examples.repositories[1]=... # Add more as needed
```

**Environment variable override:**
```bash
STARTER_EXAMPLES_REPOSITORIES=operaton/operaton-examples,acme/operaton-samples
```

**Cache:**
```properties
starter.examples.cache.dir=/path/to/cache          # Default: ${java.io.tmpdir}/operaton-starter/examples-cache
starter.examples.cache.maxSizeMb=512               # Default: 512 MB, LRU eviction
starter.examples.maxDownloadSizeMb=50              # Max uncompressed ZIP size
```

## Success Metrics (v1)

- ≥ 3 community examples in `operaton/operaton-examples` within 90 days of release
- ≥ 1 third-party repo registered within 6 months
- Examples ZIP download used ≥ as often as use-case generation within 6 months
- Zero increase in starter startup failures
- ≤ 1% of `/api/v1/metadata` responses degraded (examples list empty despite configured sources)

## Open Questions / Deferred

- **(Open Q-1)** Should refresh endpoint require authentication? v1 default: no. Revisit if starter exposed to untrusted networks.
- **(R-1)** GitHub rate-limiting mitigation: Consider caching validation results to avoid re-validating on every startup.

## Questions?

- **"Why is X designed this way?"** → Check `.decision-log.md` for rationale
- **"How do I implement Y?"** → Start with `prd.md` section 5 (FRs), then `addendum.md` for file-level notes
- **"What should my team focus on?"** → See [Team Handoff](docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/HANDOFF.md)

---

**PRD finalized 2026-06-15. Ready for implementation.**
