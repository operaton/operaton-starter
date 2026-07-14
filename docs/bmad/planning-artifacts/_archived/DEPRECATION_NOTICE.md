# Archived Planning Artifacts

## Consolidated PRD (2026-07-14) — supersedes everything below

`docs/bmad/planning-artifacts/prds/prd-operaton-starter-2026-07-14/prd.md` is now the single source of truth for operaton-starter product scope. It merges the master PRD and four feature PRDs listed below into one document with globally renumbered FR/NFR IDs; see its `addendum.md` §A for the full traceability mapping back to each archived doc's original FR IDs.

**Archived on 2026-07-14, folded into the consolidated PRD:**
- `prd-operaton-starter-master-2026-03-27/prd.md` — master product PRD
- `prd-operaton-starter-2026-06-06/prd.md` — Database Selection for Generated Projects
- `prd-operaton-starter-2026-06-08/prd.md` — UI Enhancements (Navigation, Tag Colors, Footer, Project Configuration)
- `prd-operaton-starter-examples-gallery-2026-06-13/prd.md` — Examples Gallery: Remote Example Repositories
- `prd-operaton-starter-2026-07-07/prd.md` — Examples Gallery: Repository-Wide Descriptor Discovery

**Correction to the note below:** this notice's claim that "use cases removed from operaton-starter codebase" and "operaton-starter now serves as a project generator only" was verified false during the 2026-07-14 consolidation — the four built-in use cases (`useCaseExamples`) are still fully present in code alongside the Examples Gallery (`examples`). The consolidated PRD §5.8 and §9 record the corrected status: Use Cases are deprecated and sunset-planned, not yet removed. See Open Item OI-1 there for the actual migration/removal plan.

## UC Enhancements PRD (2026-06-06) — DEPRECATED

**Status:** Archived on 2026-06-15  
**Reason:** The four use cases (UC-01 Leave Request, UC-02 Loan Application, UC-03 Incident Management, UC-04 Order Fulfillment) have been migrated to the `operaton/operaton-examples` repository. This PRD is no longer applicable to operaton-starter.

**What Changed:**
- Use cases removed from operaton-starter codebase
- Use cases now maintained in operaton-examples repository
- operaton-starter now serves as a project generator only
- Examples Gallery (separate PRD, 2026-06-13) provides curated example browsing and downloading

**For Historical Reference:**
- Original PRD: `prd-operaton-starter-uc-enhancements-2026-06-06/prd.md`
- Decision log: `prd-operaton-starter-uc-enhancements-2026-06-06/.decision-log.md`

**If You Need These Use Cases:**
They are now located in the operaton-examples repository with continued support and enhancements there.
