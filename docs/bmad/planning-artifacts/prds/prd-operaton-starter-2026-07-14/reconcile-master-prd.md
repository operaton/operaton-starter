# Reconciliation: Master PRD vs. Consolidated PRD (2026-07-14)

Comparison of `docs/bmad/planning-artifacts/prd.md` (master, 2026-03-27 / updated 2026-07-14) against `docs/bmad/planning-artifacts/prds/prd-operaton-starter-2026-07-14/prd.md` + `addendum.md`.

Scope of this report: **gaps only** — content in the master PRD not represented, even paraphrased, in the consolidated set. Items intentionally merged/renumbered/reworded are not flagged. OI-5 ("one-dependency constraint lifted") is confirmed already tracked as an open item in the consolidated doc §8 — not re-flagged below.

## Confirmed: Deferred/Roadmap section survived intact

§10 of the consolidated PRD reproduces Phase 2/3/4 exactly (Camunda 7 Migration, telemetry, CLI full prompt mode, "What's New" banner; Engine Plugin, Backstage plugin, multi-module, tested stack snapshots; Connector, community config gallery, git push, formula/recipe system, upgrade-aware Maven plugin) plus the MCP-removal note. No loss here.

## Gaps Found

### 1. "Innovation & Novel Patterns" section entirely absent
The master PRD's dedicated section (lines 231–262) is not represented in any form:
- **Detected Innovation Areas** — "Ecosystem First-Mover" framing, "Unified Generation Engine Across All Channels" (explicit comparison: start.camunda.com has no API, code.quarkus.io has no Maven archetype parity), "Migration as a Project Type" (positioning migration as tool-assisted rather than manual/FAQ).
- **Market Context & Competitive Landscape** — start.spring.io as UX reference, code.quarkus.io as extension-discovery UX reference, explicit critique of start.camunda.com (no API, no CLI, no migration mode, stale defaults), note that the repo spans Java + JavaScript.
- **Validation Approach** — the two stated validation methods: (a) run the 15-combination CI matrix across all three channels simultaneously and diff output for byte/structural identity; (b) validate the migration project type against a known Camunda 7 sample project, target ≥80% automatic dependency-substitution success.
- **Risk Mitigation table** (Innovation section, 3 rows) — `migrate-from-camunda-recipe` going unmaintained (mitigation: fork under Operaton org), archetype engine latency risk to the ≤1s target (mitigation: CI benchmark, pre-compile archetypes), community template quality drift (mitigation: contribution checklist + CI matrix validation).

None of this is paraphrased elsewhere — the consolidated doc's §3 "Product Architecture" and §4 "Success Metrics" cover *decisions* and *targets* but not the competitive rationale or validation/risk-mitigation methodology behind them.

### 2. Phase-level Risk Mitigation table (Scoping section) absent
Master PRD's "Project Scoping & Phased Development → Risk Mitigation" table (4 rows: spec drift, template combinatorics, solo-developer resourcing, low initial adoption market risk) has no counterpart. The consolidated doc's "Counter-metrics to watch" (§4) covers a different, narrower set of forward-looking risks (combinatorial CI slowdown, Examples Gallery growth, over-aggressive rate limiting) — it does not carry forward the spec-drift, resourcing, or market-adoption risks from the master.

### 3. User Journeys (narrative form) dropped to a persona table
Master PRD's five full journeys (Marcus, Elena, Thomas, Priya, Klaus — each with Opening Scene/Rising Action/Climax/Resolution and a "Capabilities revealed" list) and the "Journey Requirements Summary" table are reduced in the consolidated doc to a single-row-per-persona table (§2) listing only profile + need. This is a legitimate compression for a "consolidated requirements" doc, but two pieces of *content*, not just prose, are lost and not recoverable from the table:
- Elena's journey is the only place the **80% automatic dependency-substitution** success bar and the OpenRewrite-recipe-driven `MIGRATION.md` mechanic are described end-to-end (also missing per Gap 1's Validation Approach item — same content, two locations in the master).
- Concrete UX/behavioral specifics embedded only in journey prose: e.g., Marcus's README knows his configured server port and shows port-specific troubleshooting instructions; Thomas's inline help copy example distinguishing Process Archive from Process Application ("engine-agnostic deployable... use this when your organisation runs a shared Operaton server"). These are illustrative rather than normative, but they are the only source for a couple of FRs' intent (e.g., FR33/FR55's "troubleshooting section... port-specific instruction" — the consolidated FR55 mentions ports generically but drops the master's explicit "Troubleshooting section with common startup failure modes" framing from FR33/Marcus's journey).

### 4. Use-case example technical detail (UC-01–UC-04) substantially thinned
The master PRD's per-use-case detail block (lines 442–481: the summary table with BPMN concept/user roles per example, and four detailed subsections with explicit BPMN flow notation, DMN specifics, and testing constraints) is not carried into the consolidated PRD beyond the generic FR70–73 in §5.8. Specifically absent, with no paraphrase:
- The **per-example BPMN flow descriptions** (e.g., UC-02's `Start → ServiceTask(credit score check) → BusinessRuleTask(DMN: risk assessment) → Gateway → ...`) and named user personas per use case (`alice`/`bob`/`carol`, `jack`/`kate`, `henry`/`iris`, `dave`).
- UC-02's **DMN specifics**: `risk-assessment.dmn` with named inputs/output and `FIRST` hit policy, and the explicit note that DMN engine support must be declared (not assumed transitive).
- UC-03's **timer/ClockUtil testing constraint**: `ClockUtil` used to advance the engine clock, with mandatory `ClockUtil.reset()` after each timer-dependent test to prevent cross-test pollution. (The consolidated FR72/NFR area mentions the H2/test-profile pattern generally but not this specific API-level testing requirement.)
- UC-04's **WireMock readiness constraint**: test must wait for `/__admin/mappings` health check before the first service-task invocation.
- The **deferred use case** `document-approval` (MinIO-based, explicitly named and scoped as post-MVP with its own rationale) has no mention anywhere in the consolidated set.
- Master's **FR78** (each use case's metadata entry carries its own `templateManifest` so the File Structure Preview shows the use-case-specific BPMN rather than the generic skeleton) has no corresponding FR in the consolidated doc — FR79 (§5.9, Examples Gallery) covers manifest-driven metadata for the *new* Examples Gallery entries, but the legacy Use Cases' per-example template manifest requirement (FR78) is not mapped in addendum.md's traceability table (§A jumps FR70–73 → master FR68/69/70/74/73, skipping FR78 entirely) and isn't restated in §5.8's FR70–73.

### 5. A few standalone FRs/NFRs with no traceability entry
Cross-checking addendum.md §A against the master's FR list, the following master IDs are not visibly mapped to any consolidated FR and are not on the retired list (FR31/32/48/52/NFR15): **FR78** (use-case template manifest, see Gap 4) and **NFR4** (the starter's own repo running Dependabot/Renovate on itself to catch Operaton version bumps — distinct from generated-project dependency updates, FR57). NFR4 does not appear in the consolidated NFR list (§7) under any paraphrase; the closest consolidated NFR (NFR16, version-currency-adjacent) doesn't cover self-application of dependency-update tooling.

## Not flagged (confirmed present or legitimately merged)

- OI-5 "one-dependency constraint lifted" — present as open item OI-5 in consolidated §8, as expected; not re-flagged.
- Executive Summary, Success Criteria/Metrics, Product Scope MVP/Growth/Vision, core FR1–FR69 content, NFR1–NFR22, Monorepo structure, Metadata-as-source-of-truth, Release & Distribution FRs, self-hosting FRs — all represented (renumbered/merged) in the consolidated doc.
- Phase 2–4 deferred roadmap — confirmed intact verbatim in §10.
