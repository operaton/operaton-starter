# Reconciliation — UI Enhancements PRD (2026-06-08) vs. Consolidated PRD (2026-07-14)

Source: `docs/bmad/planning-artifacts/prds/prd-operaton-starter-2026-06-08/prd.md`
Target: `docs/bmad/planning-artifacts/prds/prd-operaton-starter-2026-07-14/prd.md` + `addendum.md`

## Method

Cross-checked every F1–F5 requirement and NFR-1/NFR-2 and Out-of-Scope bullet from the source
PRD against the consolidated PRD's body (§5.2, §5.3, §7) and the FR traceability table in
`addendum.md §A`. Confirmed represented items (renumbered/merged, not gaps):

- FR-1.1–FR-1.4 → FR10–FR12 (traceability table, `addendum.md §A`) — represented.
- FR-2.1–FR-2.4 → FR31–FR32 — represented.
- FR-3.1–FR-3.2 → FR30 — represented.
- FR-4.1–FR-4.4 → FR46 — represented.
- FR-5.1–FR-5.6 → FR43 (merged with Examples Gallery's FR-D7) — represented.
- NFR-1 (Tailwind/design-system consistency) → paraphrased in consolidated NFR19 ("Web UI
  visual design stays consistent with the `operaton.org` / `docs.operaton.org` design system")
  — represented, treated as a broadened restatement, not a gap.

## Gaps Found

### 1. NFR-2 — no manual step in standard Maven build for `VITE_APP_VERSION` injection

Source (F4, NFR-2): *"`VITE_APP_VERSION` injection SHALL require no manual step in the standard
Maven build (`mvn package`, `mvn spring-boot:run`)."*

The consolidated PRD's FR46 (footer version display) only restates the *display* requirements
(format, suffix stripping, fallback to no-version-string). The build-time mechanism is described
in FR46's own text ("injected at build time... sourced from the Maven POM") only insofar as it
appears in the *source* FR-4.3 — but FR-4.3 itself (the Vite env-var injection mechanism, working
in both `vite dev` and `vite build` without manual update) is **not** picked up by any consolidated
FR, and the "no manual step" NFR constraint is absent from the consolidated NFR list (§7) and from
`addendum.md`. This is a testable operational requirement (automation of the build pipeline step)
that is not merely a paraphrase of FR46's display-format wording — it is missing.

### 2. Out of Scope — "Changes to generated project template content or structure"

Not present anywhere in the consolidated PRD's Out-of-Scope/non-goals statements (§5.9 non-goals
are Examples-Gallery-specific; §10's "explicitly out of scope" is MCP-specific only).

### 3. Out of Scope — "New project types or use cases"

Not present in consolidated PRD. (Note: §10 Deferred/Roadmap does discuss future project types,
but that is forward-looking roadmap, not a restatement of this PRD's boundary that new types/use
cases were out of scope *for this UI-enhancements effort*.)

### 4. Out of Scope — "Accessibility audit beyond WCAG AA for tag chip colors"

Not present. The consolidated PRD's NFR14 (WCAG 2.1 AA, axe-core, manual keyboard testing) is a
general product-wide accessibility NFR, not a scoping statement that a broader accessibility audit
of tag chips specifically was excluded from this feature's scope.

### 5. Out of Scope — "Dark mode support for tag chip colors"

Not present anywhere in the consolidated PRD.

### 6. Out of Scope — "Validation of Maven coordinate fields beyond the version field (groupId, artifactId format)"

Not present anywhere in the consolidated PRD. FR9–FR12 describe groupId/artifactId defaults and
editability and the version-field validation, but nowhere states that groupId/artifactId *format*
validation remains explicitly out of scope.

## Not Gaps (for completeness)

- FR-1.1–FR-1.4, FR-2.1–FR-2.4, FR-3.1–FR-3.2, FR-4.1–FR-4.4 (excluding the NFR-2 mechanism
  detail above), FR-5.1–FR-5.6, and NFR-1 are all represented in the consolidated document, per
  the traceability table in `addendum.md §A`.
