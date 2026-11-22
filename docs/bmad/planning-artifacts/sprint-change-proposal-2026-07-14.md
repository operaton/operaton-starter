# Sprint Change Proposal — Archetype: Embedded Generation Client

**Date:** 2026-07-14
**Project:** operaton-starter
**Trigger:** "The archetype must not depend on the REST service, it should embed the template module and call it directly."
**Scope classification:** Moderate

## 1. Issue Summary

`architecture.md` designated `RestGenerationClient` (HTTP call to `POST /api/v1/generate`) as the MVP implementation of the `GenerationClient` strategy interface in `starter-archetypes`, with `EmbeddedGenerationClient` (direct in-process call to `starter-templates`) deferred to Phase 2. This was never implemented — `starter-archetypes` is currently skeleton-only (`pom.xml`, `README.md`, one empty `package-info.java`; no `GenerationClient`, no client implementation, no archetype descriptor, no tests).

The product owner determined this MVP/Phase-2 split was backwards: the archetype must call `starter-templates` directly and must never depend on a running REST service.

**Supporting evidence found during impact analysis:**
- `starter-archetypes/pom.xml` already declares a dependency on `starter-templates` only — it never depended on `starter-server`. The REST-first decision was never reflected in the module's actual dependency graph.
- `architecture.md:68` already bans calling `mvn archetype:generate` (or any Maven subprocess) at runtime for performance reasons (NFR1, ≤1s); the same performance argument applies against a network hop to a REST service.
- Story 1.2's own acceptance criteria (epics.md:280) require the generation engine to be "embeddable in Maven archetypes" — embedding was the intended design from the engine side; the archetype side just never caught up.
- No epic or story for `starter-archetypes` exists anywhere (`epics.md`'s 8 epics, `sprint-status.yaml`'s 15 epics) — this is corrected as part of this proposal, not a separate gap.

**Issue category:** Misunderstanding of original requirements (an architectural decision was recorded inconsistently with the module's own stated purpose and its own dependency graph).

## 2. Impact Analysis

**Epic impact:** No existing epic requires modification — none currently covers `starter-archetypes`. A new epic (Epic 9 in `epics.md` / `epic-16` in `sprint-status.yaml`) is added. No ripple into Epic 1 (engine/API) or Epic 5 (CLI) — neither references archetype generation mode.

**Artifact conflicts:**
- **PRD:** none. The consolidated PRD (`prds/prd-operaton-starter-2026-07-14/prd.md`) states only that the Maven archetype channel "invokes one shared generation engine" — it does not commit to a transport mechanism. Embedding satisfies this requirement more directly than a REST hop would.
- **Architecture:** real conflicts in `architecture.md` at: Technical Constraints & Dependencies (§, line ~70), System Context Diagram (~108-144), Implementation Sequence (~276-284), Deferred Decisions (~304), Decision Impact Analysis / Cross-component Dependencies (~504-520), Monorepo Structure tree (~822-834), Architectural Boundaries diagram (~895-934), submodule README spec (~1009-1014), ADR #7 (~1070), Areas for Future Enhancement (~1129). All corrected in place (Section 4 below) plus a new Amendment section added for audit trail, consistent with this document's existing convention (see the 2026-06-13 Examples Gallery amendment).
- **Epics:** `epics.md:167` (MVP/Phase-2 language) and `epics.md:321` (Story 1.3 AC says archetype "calls the API") both corrected.
- **UX:** none — the archetype module has no UI surface.
- **Other artifacts (CI, deployment, docs):** none currently reference archetype generation mode; nothing else to update.

**Positive side effect:** removing the REST dependency also removes the "developed last, gated on a deployed `starter-server`" sequencing constraint that `architecture.md` had imposed on `starter-archetypes`. The module now only needs `starter-templates`, which has been buildable since early in the project — it can move earlier in any future implementation sequencing if desired.

## 3. Recommended Approach

**Selected: Option 1 — Direct Adjustment.**

- No code exists yet for `starter-archetypes` beyond the bare module skeleton — this is authoring the epic/story/architecture text correctly the first time, not un-doing built work. Rollback (Option 2) is not applicable; nothing to roll back.
- PRD MVP scope is unaffected (Option 3, MVP Review, not applicable) — the archetype channel remains in scope, unchanged in what it delivers to the user, only how it's implemented internally.
- Effort: Low (documentation-only changes in this proposal; implementation is a small, well-scoped follow-on story). Risk: Low (no existing code or stories to conflict with; dependency graph already supports the corrected direction).

## 4. Detailed Change Proposals

### 4.1 Architecture (`docs/bmad/planning-artifacts/architecture.md`)

| Location | Before | After |
|---|---|---|
| Technical Constraints & Dependencies | "MVP: `RestGenerationClient` (HTTP call to `/api/v1/generate`). Phase 2+: `EmbeddedGenerationClient`..." | "`starter-archetypes` calls `GenerationEngine.generate()` in `starter-templates` directly, in-process. No REST-based client — no network dependency, no running server required." |
| System Context Diagram | `DEV_MVN -->\|HTTP via RestGenerationClient\| ARCHETYPES`; `ARCHETYPES -->\|MVP: REST\| SERVER`; `ARCHETYPES -.->\|Phase 2: EmbeddedGenerationClient\| TEMPLATES` (dotted) | `DEV_MVN -->\|mvn archetype:generate\| ARCHETYPES`; REST edge to SERVER removed; `ARCHETYPES -->\|Embedded, in-process\| TEMPLATES` (solid, current) |
| Implementation Sequence (Confirmed) | Step 4 "Generate CLI client stub for `starter-archetypes` from frozen spec"; Step 6 "Bootstrap `starter-archetypes` (`RestGenerationClient` MVP) last" | Step 4 removed (nothing to generate from the spec — no REST client); archetypes no longer gated "last" |
| Deferred Decisions (Post-MVP) | "`EmbeddedGenerationClient` for offline `mvn archetype:generate` (Phase 2)" | removed — not deferred, it's the only approach |
| Decision Impact Analysis / Cross-component Dependencies | "Implement `starter-archetypes` `RestGenerationClient` (MVP) last"; "`starter-archetypes` depends on `starter-server` being deployed (REST target); developed last" | "Implement `EmbeddedGenerationClient`"; "`starter-archetypes` depends only on `starter-templates` — no longer gated on `starter-server` deployment" |
| Monorepo Structure tree | `RestGenerationClient.java ← MVP: calls /api/v1/generate`; test `RestGenerationClientTest.java` | `EmbeddedGenerationClient.java ← MVP: calls GenerationEngine.generate() directly, in-process`; test `EmbeddedGenerationClientTest.java` |
| Architectural Boundaries diagram | `ARCH["starter-archetypes\nRestGenerationClient"]`; `ARCH --> CTRL` | `ARCH["starter-archetypes\nEmbeddedGenerationClient"]`; rewired `ARCH --> ENGINE` |
| `starter-archetypes/README.md` spec | Role "enables `mvn archetype:generate` to call the REST API"; prerequisite "a running `starter-server` instance"; usage example with `-DserverUrl=...` | Role "enables `mvn archetype:generate` to call `starter-templates` directly, in-process"; no running-server prerequisite; usage example drops `-DserverUrl` |
| ADR #7 | "`GenerationClient` strategy interface (MVP: REST → Phase 2: embedded)" | "`GenerationClient` strategy interface (embedded only — direct call to `starter-templates`; no REST-based client)" |
| Areas for Future Enhancement | "Phase 2: `EmbeddedGenerationClient` for offline `mvn archetype:generate`" | removed |

**New Amendment section** (appended after the 2026-06-13 Examples Gallery amendment, same document convention):

> ## Amendment 2026-07-14 — Archetype: Embedded Generation Client (Course Correction)
>
> **Driver:** Sprint change proposal, 2026-07-14 — `starter-archetypes` must not depend on the REST service; it embeds `starter-templates` and calls the generation engine directly.
>
> This amendment **reverses** a core decision, not additive scope: the original MVP/Phase-2 split designated `RestGenerationClient` as MVP and `EmbeddedGenerationClient` as Phase 2+. That is now inverted — embedded is the only implementation; the REST-based client is dropped from scope entirely, not deferred.
>
> **Why:** `starter-archetypes/pom.xml` already depended only on `starter-templates`, never on `starter-server` — the REST-first decision was never reflected in the actual module dependency graph. Embedding also removes the "developed last, gated on a deployed server" sequencing constraint — the module can now be built any time after `starter-templates` exists.
>
> **What changed:** every reference to `RestGenerationClient` as the MVP/current archetype client is corrected in place throughout this document (Technical Constraints, System Context Diagram, Implementation Sequence, Cross-component Dependencies, Architectural Boundaries diagram, Monorepo Structure tree, `starter-archetypes/README.md` spec, ADR #7, Areas for Future Enhancement).
>
> **Open question carried into story-writing:** how `mvn archetype:generate` actually triggers `EmbeddedGenerationClient` at runtime (custom Mojo? post-generate hook?) is not resolved here — see Story 9.1's note.

### 4.2 Epics (`docs/bmad/planning-artifacts/epics.md`)

| Location | Before | After |
|---|---|---|
| Additional Requirements (Architecture) bullet | "`GenerationClient` interface in `starter-archetypes`: MVP uses `RestGenerationClient` (HTTP); Phase 2+ adds `EmbeddedGenerationClient` (direct library call, no network) for offline archetype use" | "`GenerationClient` interface in `starter-archetypes`: calls `starter-templates` directly, in-process — no REST client, no network dependency" |
| Story 1.3 Acceptance Criteria | "any channel (web, CLI, archetype) calls the API" | "any channel calls the shared engine — web/CLI/curl via the API, archetype via a direct in-process call to the same engine" |

**New epic added** (fills the previously-nonexistent archetype coverage):

> ### Epic 9: Maven Archetype Integration
>
> The `starter-archetypes` module provides `mvn archetype:generate` as a first-class generation channel, calling the shared generation engine (`starter-templates`) directly and in-process — no network dependency, no running server required.
> **FRs covered:** FR42 (channel consistency)
>
> ### Story 9.1: Implement GenerationClient + EmbeddedGenerationClient
>
> As a developer using `mvn archetype:generate`,
> I want the archetype to call the generation engine directly, in-process,
> So that generating a project requires no running server, no network access, and produces output identical to every other channel.
>
> **Acceptance Criteria:**
>
> **Given** the `GenerationClient` interface defined in `starter-archetypes`
> **When** `EmbeddedGenerationClient` is invoked with a `ProjectConfig`
> **Then** it calls `GenerationEngine.generate(ProjectConfig)` in `starter-templates` directly — no HTTP client, no network call, no dependency on `starter-server` being built or running
>
> **Given** `starter-archetypes/pom.xml`
> **When** dependencies are reviewed
> **Then** it depends only on `starter-templates` (already the case) — no dependency on `starter-server` is introduced
>
> **Given** the same `ProjectConfig` generated via `EmbeddedGenerationClient` and via `POST /api/v1/generate`
> **When** the two outputs are compared
> **Then** they are byte-for-byte identical (generation timestamps excepted)
>
> **Given** `EmbeddedGenerationClientTest`
> **When** this story is implemented
> **Then** it replaces the originally-planned `RestGenerationClientTest` — no REST-calling test class exists in this module
>
> [NOTE FOR PM/ARCHITECT]: standard Maven archetypes fill a static `archetype-resources/` tree via Velocity substitution — they don't normally invoke arbitrary Java calling `GenerationEngine.generate()`. How `mvn archetype:generate` actually triggers `EmbeddedGenerationClient` (custom Mojo? post-generate hook? something else) is NOT resolved by this story and should be pinned down before implementation starts, not during it.

### 4.3 Sprint Status (`docs/bmad/implementation-artifacts/sprint-status.yaml`)

New entry, status `backlog`:

```yaml
  # ─────────────────────────────────────────────────────────────────────────
  # Epic 16: Maven Archetype Integration
  # ─────────────────────────────────────────────────────────────────────────
  epic-16: backlog

  16-1-implement-generationclient-embeddedgenerationclient: backlog
```

## 5. Implementation Handoff

**Scope classification: Moderate** — backlog reorganization (new epic/story) plus a documentation correction across two planning artifacts. No code exists yet, so this is not a Developer-agent-only task, but it's also not a fundamental replan requiring fresh PM/Architect strategy work — the architecture's own dependency graph already supported the corrected direction.

**Routed to:** Product Owner / Developer, per this proposal.

**Responsibilities:**
- All document edits in Section 4 are pre-approved (reviewed incrementally with the product owner during this session) — apply directly to `architecture.md`, `epics.md`, and `sprint-status.yaml`.
- Before Story 9.1 implementation begins: resolve the open question flagged in the story's `[NOTE FOR PM/ARCHITECT]` — how `mvn archetype:generate` triggers `EmbeddedGenerationClient` at runtime. This is a prerequisite design decision, not an implementation detail to improvise mid-story.
- `bmad-create-story` to produce the full story-context file for Story 9.1 once the open question is resolved.
- `bmad-dev-story` to implement.

**Success criteria:** `starter-archetypes` module builds and tests green with `GenerationClient` + `EmbeddedGenerationClient` implemented, zero dependency on `starter-server` in `pom.xml`, and output byte-identical to the REST/CLI channels for the same `ProjectConfig`.
