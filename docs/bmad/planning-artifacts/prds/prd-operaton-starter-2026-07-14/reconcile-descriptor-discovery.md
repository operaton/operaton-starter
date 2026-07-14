---
title: "Reconciliation — Descriptor Discovery PRD vs. Consolidated 2026-07-14 PRD"
created: 2026-07-14
---

# Reconciliation: Descriptor Discovery Amendment vs. Consolidated PRD

## Intro

This document compares the source amendment PRD, **"Examples Gallery: Repository-Wide
Descriptor Discovery"**
(`docs/bmad/planning-artifacts/prds/prd-operaton-starter-2026-07-07/prd.md`), against the
two consolidated target documents dated 2026-07-14:

- `docs/bmad/planning-artifacts/prds/prd-operaton-starter-2026-07-14/prd.md`
- `docs/bmad/planning-artifacts/prds/prd-operaton-starter-2026-07-14/addendum.md` (§C in
  particular, "Examples Gallery — Manifest Schema Reference")

All three files were read in full. The goal was to confirm every FR, NFR, backward-compatibility
note, and resolved open question from the source amendment is represented — in any wording,
numbering, or merged form — somewhere in the target pair. Only genuine substance gaps are
flagged; renumbering, rewording, or merging of requirements is not treated as a gap.

## Gaps Found

### Gap 1 — Truncated Trees API response handling (source FR-1.4, and OQ-1 resolution)

- **Source location:** §5 "FR-1 Repository-Wide Scanning," item FR-1.4; resolution restated
  in §8 "Resolved Questions," OQ-1.
- **Source text:** "When the GitHub Trees API returns `truncated: true` (repository exceeds
  the API's single-response capacity), the system MUST log a warning identifying the source
  repository and process only the descriptors visible in the partial response. It MUST NOT
  drop the source entirely or fail silently." OQ-1's resolution adds: "Warning + partial
  processing is acceptable for v1. Expected example repositories are small; a non-recursive
  fallback is deferred to a future enhancement if needed."
- **Search result:** Searched both `prd.md` and `addendum.md` for "truncated," "truncation,"
  "partial response," and related terms. The only Trees API reference in the target is
  addendum.md §C: "discovered via one GitHub Trees API call per source, `?recursive=1`" — a
  bare mention of the API call, with no discussion of the truncation edge case, its handling,
  or the accepted v1 risk trade-off. Absent from both files.
- **Why it matters:** This is a genuine operational edge case (large repos) with an explicit
  MUST-not-fail-silently requirement and a deliberate risk-acceptance decision (OQ-1). Neither
  the requirement nor the decision survives into the consolidated docs.

### Gap 2 — `path` field validation rules (source FR-4.3)

- **Source location:** §5 "FR-4 Optional `path` Field," item FR-4.3.
- **Source text:** "Existing `path` validation rules remain in force when a value is
  provided: no leading `/`, no `..` segments, no null bytes, no empty string after trimming."
- **Search result:** Searched both target files for "leading," "`..`," "null byte," and
  "trim." `addendum.md` §C documents only the *resolution/default* behavior of `path`
  ("optional — defaults to the descriptor's own directory, resolved as
  `<descriptorDir>/<path>`") — no validation constraints on a supplied value appear anywhere
  in either file.
- **Why it matters:** This is a security/robustness constraint (path traversal and injection
  prevention) that is entirely unstated in the target, not just reworded.

### Gap 3 — Per-descriptor failure detail surfaced in outcome (source FR-6.2)

- **Source location:** §5 "FR-6 Per-Descriptor Error Isolation," item FR-6.2.
- **Source text:** "Per-descriptor failures MUST be surfaced in the source's outcome detail
  (descriptor path + failure reason) so operators can diagnose issues without digging into
  server logs."
- **Search result:** Target `prd.md` FR77 covers only the coarser, whole-source granularity:
  "A single source's manifest failure ... never blocks startup or other sources; it is
  skipped with a logged reason." NFR25 covers structured logging ("source, example ID,
  outcome, duration") but is about log content, not about a per-descriptor (path + reason)
  breakdown surfaced in a source's outcome structure for operator-facing diagnosis without
  reading logs. FR-6.1 (a failing descriptor must not block sibling descriptors in the same
  repo) is arguably implied by FR77's "never blocks ... other sources," but that source-level
  statement doesn't establish the finer per-descriptor isolation or the outcome-surfacing
  behavior FR-6.2 specifically requires.
- **Why it matters:** The source draws a specific distinction between descriptor-level and
  source-level failure granularity; the target only has the source-level concept.

### Gap 4 — API-efficiency NFR formula (source NFR-1)

- **Source location:** §6 "Non-Functional Requirements," NFR-1.
- **Source text:** "For a repository with N descriptors, the total GitHub API calls per
  source refresh is 1 (tree enumeration) + N (per-descriptor fetches). For single-descriptor
  repositories this is one extra round trip ... this overhead is acceptable."
  compared to today.
- **Search result:** Target mentions only the single tree-enumeration call
  (addendum.md §C), never the additive N-descriptor-fetch cost or the explicit acceptance of
  the one-extra-round-trip overhead versus the pre-amendment baseline.
- **Why it matters:** This is a quantified efficiency/cost trade-off the source explicitly
  accepted; the acceptance and the formula are both absent from target.

### Gap 5 — Timeout/retry reuse for the tree-enumeration call (source NFR-3)

- **Source location:** §6 "Non-Functional Requirements," NFR-3.
- **Source text:** "The existing per-call timeout and `SourceUnavailable` handling apply to
  the new tree-enumeration call as well as individual descriptor fetches."
- **Search result:** Target `prd.md` NFR4 and NFR6 make general statements about per-source
  timeouts and non-blocking failure of external services, but neither states that the
  pre-existing timeout/`SourceUnavailable` mechanism is explicitly extended to cover the new
  tree-enumeration call as a distinct step. `SourceUnavailable` as a named concept does not
  appear in either target file.
- **Why it matters:** This is a specific implementation-continuity guarantee (reusing
  existing error-handling machinery for a new call site) that isn't stated even generically
  in the target.

### Gap 6 — Descriptor-discovery integration test coverage list (source §9 Success Metrics)

- **Source location:** §9 "Success Metrics."
- **Source text:** "Integration tests cover: `.yml` extension, `.yaml` extension,
  per-directory descriptor with implicit `path`, multiple descriptors in one repo,
  duplicate-id handling, and failed-descriptor isolation."
- **Search result:** No equivalent test-coverage enumeration for descriptor discovery exists
  in either target file. `addendum.md` §B has a "Generation test matrix" but it is scoped to
  database-option combinations, not descriptor/manifest discovery. The individual behaviors
  (`.yml` precedence, duplicate-id skip) are stated as functional facts in addendum.md §C,
  but the source's framing of these as a required *test-coverage checklist* — including
  "failed-descriptor isolation" as a distinct test target — is not carried forward.
- **Why it matters:** A checklist of what must be test-covered is a distinct, actionable
  commitment beyond stating the behavior itself; it's absent.

## Confirmed Covered

| Source item | Target coverage |
|---|---|
| **FR-1.1–1.3** (repository-wide scan via GitHub Trees API `recursive=1`, exact filename match for `.operaton-starter.yml`/`.yaml`) | `prd.md` FR75 ("Manifests may live at any depth in the repository tree, not only at the root, and a repository may publish multiple manifests"); `addendum.md` §C ("discovered via one GitHub Trees API call per source, `?recursive=1`") |
| **FR-1.4 / OQ-1** (truncated-tree handling) | **NOT COVERED — see Gap 1** |
| **FR-2** (multiple descriptors per repo; independent fetch/parse; combined flat result; duplicate-id skip, first-discovered wins) | `prd.md` FR75 (multiple manifests per repo); `addendum.md` §C ("a repo may have multiple descriptors; duplicate example `id`s within a repo are skipped (first-discovered wins) with a warning") |
| **FR-3** (descriptor-relative path resolution; `<descriptorDir>/<path>`; root descriptor = unchanged behavior) | `prd.md` FR75 ("each example's content directory defaults to the manifest's own directory when no explicit path is given"); `addendum.md` §C ("`path` ... resolved as `<descriptorDir>/<path>`") |
| **FR-4.1–4.2** (`path` optional, defaults to `.`) | `prd.md` FR75; `addendum.md` §C ("path (optional — defaults to the descriptor's own directory...)") |
| **FR-4.3** (path validation rules) | **NOT COVERED — see Gap 2** |
| **FR-5** (`.yml`/`.yaml` both recognized; `.yml` wins on collision, warning logged) | `prd.md` FR74 ("publish an `.operaton-starter.yml` (or `.yaml`) manifest"); `addendum.md` §C ("if both `.yml` and `.yaml` exist in the same directory, `.yml` wins") |
| **FR-6.1** (descriptor failure isolation, doesn't block siblings) | Partially represented at source-level granularity only via `prd.md` FR77 ("A single source's manifest failure ... never blocks startup or other sources; it is skipped with a logged reason") — the finer per-descriptor framing is not explicit |
| **FR-6.2** (per-descriptor failure detail in outcome) | **NOT COVERED — see Gap 3** |
| **FR-7.1** (documentation updates to `docs/examples-repository-format.md`) | `prd.md` FR83 ("`docs/examples-repository-format.md` documents the manifest schema, an annotated example, and instructions for getting a repository registered..."); FR74/FR75 restate the substantive content that doc must reflect |
| **NFR-1** (API call efficiency, 1+N formula) | Partially — only the single tree call is mentioned (`addendum.md` §C); the N-descriptor-fetch addition and accepted-overhead framing is **NOT COVERED — see Gap 4** |
| **NFR-2** (backward compatibility, purely additive schema change) | `prd.md` NFR20 ("Adding a database option or an Examples Gallery source is additive and backward-compatible — existing callers omitting the new fields see unchanged output/behavior") — general principle covers this case; also FR75's root-manifest-still-supported framing |
| **NFR-3** (timeout/retry reuse for tree call) | **NOT COVERED — see Gap 5** |
| **OQ-1 resolution** (truncated-tree: warn + partial process, defer non-recursive fallback) | **NOT COVERED — see Gap 1** |
| **OQ-2 resolution** (`.yml` takes precedence over `.yaml` on collision) | `addendum.md` §C ("if both `.yml` and `.yaml` exist in the same directory, `.yml` wins") |
| **§9 Success Metrics — integration test coverage checklist** | **NOT COVERED — see Gap 6** |
| **§9 Success Metrics — no regression for root-manifest repos, zero behavioral change** | `prd.md` NFR20 (additive/backward-compatible framing); FR75 (root-manifest pattern implicitly still supported since path defaulting/resolution described as backward compatible) |

No contradictions were found between source and target — all overlapping content is
consistent in substance. The six gaps above are omissions, not conflicts.
