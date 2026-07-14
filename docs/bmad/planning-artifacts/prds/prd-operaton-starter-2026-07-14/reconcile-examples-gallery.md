# Reconciliation Check — Examples Gallery PRD (2026-06-13) vs. Consolidated PRD (2026-07-14)

Quality-gate comparison performed prior to finalizing the consolidated PRD. Goal: find genuine gaps (substance dropped entirely), not renumbering/merging/paraphrasing.

## 1. Methodology

Files read in full:

| File | Lines |
|---|---|
| `prd-operaton-starter-examples-gallery-2026-06-13/prd.md` (source) | 144 |
| `prd-operaton-starter-examples-gallery-2026-06-13/addendum.md` (source) | 107 |
| `prd-operaton-starter-2026-07-14/prd.md` (consolidated) | 290 |
| `prd-operaton-starter-2026-07-14/addendum.md` (consolidated) | 83 |

Approach:
1. Read all four documents completely (no offset/limit needed — all under 300 lines).
2. Built a checklist of every FR (Groups A–E), NFR (1–7), user journey (UJ-1 to UJ-4), risk (R-1 to R-5), open question, success metric, non-goal, and the Deprecation Note from the source PRD + addendum.
3. For each checklist item, grepped and manually cross-referenced both consolidated files (`prd.md` §3, §4, §5.9, §7, §8, §9; `addendum.md` §C, §D) to find where it landed, using both exact terms and paraphrase-level reasoning.
4. Distinguished "renumbered/merged/reworded" (not a gap) from "substance absent from both files" (a gap).
5. Ran targeted greps for specific technical terms (rate-limiting hosts, telemetry, UI micro-behaviors, success-metric numbers) to confirm absence rather than relying on a single read-through.

## 2. Confirmed Gaps

### Gap 1 — GitHub-side rate-limiting risk and its mitigation (source R-1)

**Source location:** §8 Risks & Open Questions, **R-1**:
> "GitHub rate-limiting on `raw.githubusercontent.com` and `codeload.github.com`. Anonymous limits are generous for read; mitigation if hit: introduce server-side ZIP caching keyed by `(source, ref-sha, exampleId)`. Out of scope for v1. Validation HEAD requests will also count toward limits; cache validation results to avoid re-checking on every startup."

This is a specific, named risk about **outbound calls the starter makes to GitHub's infrastructure** (manifest fetch, tarball download, validation HEAD requests) potentially hitting GitHub's own anonymous rate limits, with two concrete mitigations: (a) ZIP caching (which did ship, per FR-C7/addendum §C), and (b) **caching validation HEAD-request results across startups** (which is a distinct, narrower mitigation not fully covered by the ZIP cache).

**Confirmed absent from both consolidated files.** I searched both `prd.md` and `addendum.md` for "rate limit," "GitHub rate," "codeload," "raw.githubusercontent," "anonymous limit," and "HEAD request." The only rate-limiting content in the consolidated PRD is the **starter's own inbound API rate limiting** (`prd.md` §3 "Rate limiting," FR51, NFR9, NFR10 — a per-IP token bucket on `/api/v1/generate`), which is a completely different concern (protecting the starter from callers) from R-1 (the starter itself being rate-limited by GitHub as a caller). The validation-result-caching mitigation specifically is not mentioned anywhere. While the disk ZIP cache survived (addendum §C), the GitHub-rate-limit framing as a named risk, and the validation-caching sub-mitigation, did not.

### Gap 2 — Download telemetry mechanism (source FR-C8)

**Source location:** FR Group C — Backend API, **FR-C8**:
> "**Download telemetry.** The download endpoint increments an in-process counter per `(sourceRepo, exampleId)` and exposes the snapshot via `/actuator/examples`. Counters are not persisted across restarts. No PII, no remote reporting. This is the data source for SM-3."

**Confirmed absent from both consolidated files.** Searched both files for "telemetry," "counter," "download attempt" (found only in the context of NFR25's *logging*, not counting/exposing a metric snapshot), and "/actuator/examples" (found only as a generic "download-telemetry endpoints for operators" mention in addendum §C line 71 — see note below). The specific mechanism — an in-process per-`(sourceRepo, exampleId)` counter exposed via an actuator endpoint, explicitly justified as the data source for a success metric — is not described. Addendum §C does have the phrase "actuator-style load-status and **download-telemetry endpoints** for operators," which acknowledges an endpoint exists, but the counter mechanism, its keying, its non-persistence, and its link to a success metric are gone. This is a borderline case — the endpoint's *existence* survived in name, but the metric-collection purpose (SM-3 tie-in) did not, and see Gap 3 below: SM-3 itself is also gone, so the telemetry's stated purpose has no home in the consolidated document.

### Gap 3 — Examples Gallery success metrics (source §7)

**Source location:** §7 Success Metrics:
> "- ≥ 3 community-contributed examples published in `operaton/operaton-examples` within 90 days of release.
> - ≥ 1 third-party repository registered within 6 months.
> - Examples ZIP download is used at least as often as use-case generation within 6 months (telemetry permitting).
> - **Counter-metrics:** zero increase in starter startup failures attributable to example loading; ≤ 1% of `/api/v1/metadata` responses are 'degraded,' where **degraded** means the `examples` field is empty or missing while at least one source is configured (i.e. all configured sources failed to load)."

**Confirmed absent from both consolidated files.** The consolidated PRD's §4 Success Metrics table (UI landing→ZIP, REST API generation time, compile rate, CI pass rate, version-update lag, availability) contains no Examples-Gallery-specific targets. Its "Counter-metrics to watch" paragraph only says "Examples Gallery growth outpacing the async-validation and cache-eviction budget (NFR, §7)" — a generic forward-looking caution, not the source's precise, measurable targets (3 examples/90 days, 1 repo/6 months, download-usage-vs-use-case-generation comparison, ≤1% "degraded" response definition). I searched both files for "community-contributed," "third-party repository," "90 days," "6 months," and "degraded" — none appear. This is a genuine drop of measurable success criteria, not a rewording.

## 3. Deprecation Note Reconciliation Check

**Source (`prd.md` §1, "Deprecation Note," and echoed in §9 Phasing):**
> "Built-in Use Cases have been migrated to `operaton/operaton-examples/use-cases`. The Examples Gallery **replaces** the Use Cases feature in operaton-starter, consolidating all example content into a single external registry."
>
> (§9 Phasing, v1 bullet): "**Built-in Use Cases have been migrated to `operaton/operaton-examples/use-cases`.**"

**Consolidated (`prd.md` §9 Reconciliation Notes, item 1):**
> "**Use Cases vs. Examples Gallery.** The Examples Gallery PRD (2026-06-13) states built-in Use Cases were 'migrated' and 'replaced' by the gallery. A codebase check during this consolidation found both features still fully present and coexisting (`useCaseExamples` and `examples` are both served by `GET /api/v1/metadata`; `starter-templates` still ships all four use-case templates). Decision: treat Use Cases as deprecated and sunset-planned (§5.8), Examples Gallery as their intended replacement, without asserting a completion that hasn't happened in code yet. Tracked as OI-1."

Also relevant, consolidated `prd.md` §5.8:
> "**Status: deprecated.** The built-in Use Cases feature ... predates the Examples Gallery (§5.9) and is being phased out in its favor. The code has not yet caught up with this decision — `useCaseExamples` is still served alongside `examples` in the metadata response, and the four use cases remain fully generatable — but no new functional requirements should be added against this feature. See §9 for the reconciliation history behind this call."
>
> "**Sunset plan (to be scheduled, not yet dated):** migrate remaining use-case content to `operaton/operaton-examples` (mirroring the Examples Gallery's own deprecation note for the same content), then remove the in-app generation path and the `useCaseExamples` metadata field. This PRD does not commit to a date — see Open Item OI-1 in §8."

**Judgment: this is a reasonable, well-evidenced reconciliation — not a silent drop.**

The consolidated PRD does three things right here:
1. It explicitly quotes/paraphrases the source's claim ("migrated," "replaced").
2. It states a concrete, falsifiable reason the claim doesn't hold as written (codebase check: both features coexist in `/api/v1/metadata` and in `starter-templates`).
3. It resolves the conflict with an explicit, documented decision (treat as deprecated/sunset-planned rather than already-completed-migration) and tracks the residual work as an open item (OI-1) with a stated resolution condition ("before removing the in-app Use Cases generation path").

This is the opposite of a silent drop — the topic is surfaced, disputed with evidence, and given a forward-looking resolution path. The one soft spot: the source's Deprecation Note asserted the target of migration is specifically `operaton/operaton-examples/use-cases` (a subpath), and the consolidated sunset plan preserves that target ("migrate remaining use-case content to `operaton/operaton-examples`") without the `/use-cases` subpath detail — a minor loss of precision, not a substantive gap, since the destination repository is still correctly named.

## 4. Items Verified as Present (renumbered/merged/paraphrased — NOT gaps)

- Source §1 manifest concept (`.operaton-starter.yml`, external GitHub repos, aggregation) → consolidated `prd.md` §5.9 FR74, Executive Summary §1.
- Source Non-Goals (v1): no UI repo registration, no private-repo auth, no server-side validation/execution, no per-example version pinning → consolidated `prd.md` §5.9 "Non-goals (this version)" — all four preserved verbatim in substance.
- Source UJ-1–UJ-4 (browse/download, author publishes, operator adds repo, broken repo handled) → substance folded into FR74, FR76, FR77, FR80 (capability-level, journeys not restated narratively — standard consolidation).
- FR-A1–A7 (manifest schema, root-level, required/optional fields, unknown-field tolerance, apiVersion gating) → consolidated `addendum.md` §C (full field reference), `prd.md` FR74. Note: FR75 in consolidated actually *extends* FR-A1 (manifests no longer root-only, can be at any depth, multiple manifests allowed) — a deliberate evolution, not a gap, and documented as such in addendum §C ("Descriptors may live at any repository depth... discovered via GitHub Trees API").
- FR-B1–B4 (maintainer-controlled source list, `owner/repo[@ref]` format, preconfigured `operaton/operaton-examples`, env var override) → consolidated FR76. The literal `owner/repo[@ref]` syntax detail is superseded by the Trees-API-based discovery mechanism in addendum §C — consistent with FR75's broadened manifest-location model, not a silent drop.
- FR-B5 (fetch at startup + manual refresh, no periodic auto-refresh) → consolidated FR77, and explicitly reaffirmed as a rejected alternative in addendum §D ("periodic auto-refresh... considered and rejected for this version").
- FR-B6 (raw content URL fetch mechanism) → superseded by Trees API mechanism in addendum §C (consistent evolution, see FR75 note above).
- FR-B7 (failure handling: log warning, skip, preserve previous snapshot on refresh failure) → consolidated FR77 ("skipped with a logged reason, and a manual refresh operation can retry all sources").
- FR-B8 (load-status diagnostics endpoint) → consolidated addendum §C, "actuator-style load-status ... endpoints for operators."
- FR-B9 (manual refresh endpoint, no-auth-for-v1 assumption) → consolidated addendum §C ("`POST /api/v1/examples/refresh`... no auth in this version"), and tracked as **OI-6** in `prd.md` §8 (mirrors source's Open Q-1).
- FR-B10 (ref pinning to commit SHA, applies to manifest + downloads) → consolidated FR78 ("resolved against a pinned commit... mid-session repository changes never affect a user's current gallery view").
- FR-C1 (metadata response `examples` list with computed fields) → consolidated FR79.
- FR-C2/C3 (download endpoint, ZIP of subdirectory, tarball-filter-and-repack mechanism, streaming) → consolidated FR80, addendum §C "Download mechanism."
- FR-C4 (404/502 error handling on download) → generalized into FR80/FR81 at capability level; specific status codes not restated in prd.md but the 413 case is retained in addendum §C "Size limits."
- FR-C5 (async example validation, optimistic-then-corrected `isDownloadable`) → consolidated FR81.
- FR-C6 (in-memory-only manifest, no disk persistence) → implicit in FR78/addendum §C; not separately called out but not a substantive requirement beyond "resolved at each load."
- FR-C7 (ZIP cache: path, SHA-keyed, size bound, LRU eviction) → consolidated addendum §C "Download mechanism" paragraph, in full including the 512MB default and override properties.
- FR-D1–D5, D7 (gallery UI sections, card fields, search/filter, empty state, tag chip model reuse) → consolidated FR42–FR45, FR43.
- FR-D6 (download button, disabled+warning-icon-when-unavailable, spinner, error toast) → capability substance ("unavailable... link to source repository") preserved in FR81; the specific UI micro-behaviors (spinner, tooltip, retry toast, distinct 502/504/timeout messaging) are implementation-level detail not restated — consistent with the consolidated PRD's stated policy of keeping mechanism detail in addendum/external docs, and not flagged as a hard gap.
- FR-E1–E4 (docs file, link from gallery, README link, doc as source of truth) → consolidated FR83, FR74 ("documented separately... is the source of truth, not duplicated here").
- NFR-1 (startup overhead ≤3s for N≤10, 5s per-source timeout) → consolidated NFR4 (generalized: "a few seconds... a per-source timeout bounds worst-case delay").
- NFR-2 (resilience, single-source-failure isolation) → consolidated NFR7.
- NFR-3 (public HTTPS only, no credentials, SafeConstructor YAML parsing) → consolidated NFR11, NFR12.
- NFR-4 (256KB manifest cap, 50MB per-example cap, 413 response, streaming) → consolidated addendum §C "Size limits," NFR11.
- NFR-5 (additive/backward-compatible metadata field) → consolidated NFR20.
- NFR-6 (structured logging with source/exampleId/outcome/durationMs) → consolidated NFR25 (near-verbatim).
- NFR-7 (forward-compatible unknown fields, apiVersion major-only check) → consolidated addendum §C ("Unknown fields are ignored... unknown apiVersion major versions are refused").
- R-2 (malicious manifest / billions-of-laughs YAML) → consolidated NFR11 ("safe against untrusted YAML content").
- R-3 (schema drift mitigated by docs+permissive parser+skip-with-warning) → consolidated FR74, FR77.
- R-4 (Operaton-version drift surfaced on card, no auto-blocking) → not explicitly restated as a risk in consolidated `prd.md`, but the underlying field (`operatonVersion`) is retained in addendum §C field list; the "no auto-blocking" framing is dropped as a named risk but doesn't rise to a functional gap since no behavior was actually specified beyond displaying the field.
- R-5 (broken/stale examples mitigated by async validation) → consolidated FR81.
- Open Q-1 (refresh endpoint auth, revisit later) → consolidated **OI-6**.
- §9 Phasing v2 items (UI source registration, per-example version pinning, private-repo auth, admin diagnostics UI) → consolidated FR75/FR-non-goals section ("Non-goals (this version): user-configurable repository list in the UI... per-example version pinning by the end user"), and addendum §D ("UI-driven repository registration considered and deferred to a future version").
- Rejected alternatives (addendum §D in source: periodic refresh, generic Git fetch, refresh auth, ref-pinning bypass) → consolidated addendum §D substantially mirrors these (periodic refresh rejection, UI registration deferral); generic-Git-fetch and ref-pinning-bypass rejections are not separately restated but are consistent with (not contradicted by) the consolidated design.
- Deprecation Note → see Section 3 above; substantively reconciled, not dropped.

## Summary Judgment

Three genuine gaps identified (rate-limiting-to-GitHub risk + validation-caching mitigation; download telemetry counter mechanism; Examples-Gallery-specific success metrics). Everything else checked traces cleanly to a renumbered, merged, or deliberately-evolved counterpart in the consolidated PRD or its addendum. The Deprecation Note is handled well — disputed with evidence and given a tracked resolution path (OI-1), not silently dropped.
