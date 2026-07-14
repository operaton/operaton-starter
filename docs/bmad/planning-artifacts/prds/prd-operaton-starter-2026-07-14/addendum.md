# Addendum — operaton-starter Consolidated PRD

Companion to `prd.md`. Holds implementation-level detail, rejected/considered alternatives, and traceability that would clutter the PRD's main narrative.

## A. FR Traceability — Consolidated ID → Source Document ID

| Consolidated FR | Source document | Original ID |
|---|---|---|
| FR1–FR8 | Master PRD (2026-03-27/07-14) | FR1–FR8 |
| FR9 | Master PRD | FR11 |
| FR10–FR12 | UI Enhancements (2026-06-08) | FR-1.1–FR-1.4 |
| FR13 | Master PRD | FR10 |
| FR14 | Master PRD | FR12 |
| FR15–FR23 | Database Selection (2026-06-06) | FR-1–FR-8 |
| FR24–FR28 | Master PRD | FR13, FR14, FR15, FR56, FR16 |
| FR29 | Master PRD | FR9 |
| FR30–FR32 | UI Enhancements | FR-2.1–FR-2.4, FR-3.1–FR-3.2 |
| FR33 | Master PRD | FR41 |
| FR34–FR40 | Master PRD | FR22, FR46, FR43/FR57, FR76, FR77, FR20, FR23 |
| FR41 | Master PRD | FR60 |
| FR42 | Master PRD | FR40 |
| FR43 | UI Enhancements + Examples Gallery | FR-5.1–FR-5.6, FR-D7 |
| FR44 | Examples Gallery | FR-D3, FR-D4 |
| FR45 | Examples Gallery | FR-D5 |
| FR46 | UI Enhancements | FR-4.1–FR-4.4 |
| FR47–FR51 | Master PRD | FR24–FR27 |
| FR52–FR54 | Master PRD | FR28–FR30 |
| FR55–FR65 | Master PRD | FR33, FR45 (build-tool-wrapper sense), FR34–FR36, FR44, FR75, FR58, FR59, FR69, FR71 |
| FR66–FR69 | Master PRD | FR37, FR47, FR38, FR39 |
| FR70–FR73 | Master PRD | FR68, FR69, FR70/FR74, FR73 |
| FR74–FR82 | Examples Gallery + Descriptor Discovery | Groups A–D, and FR-1 through FR-6 (discovery PRD) |
| FR83–FR84 | Examples Gallery + Master PRD | FR-E1–FR-E3, NFR22 |
| FR85–FR88 | Master PRD | FR49–FR53 |
| FR89–FR92 | (new — no prior source) | Surfaced during input-reconciliation diffs against master PRD (FR89: per-use-case `templateManifest`, originally master FR78; FR92: originally master NFR4) and the descriptor-discovery PRD (FR90: FR-1.4/OQ-1; FR91: FR-6.2) |

**Retired/struck *master-PRD-numbered* IDs, not carried forward (master PRD frontmatter, 2026-07-14):** master-PRD FR31, FR32, FR48, FR52, NFR15 — all MCP-integration-channel requirements, removed with the MCP module. These are the *original* master-PRD numbers, pre-renumbering — do not confuse with the consolidated document's own FR31/FR32/FR48/FR52/NFR15 above (Configure Now navigation, `GET /api/v1/generate` query mode, `npx operaton-starter`, and keyboard operability, respectively), which are unrelated live requirements that happen to reuse the same numbers after renumbering.

**Numbering defect fixed:** the master PRD used "FR45" twice (once for details-page routing behavior, once for build-tool-wrapper inclusion). The routing requirement is now FR32/FR35 (folded into navigation/conditional-rendering); the build-tool-wrapper requirement is now FR56.

## B. Database Selection — Technical Reference

Applies to `ProjectConfig.databaseOption` (enum: `H2` default, `POSTGRESQL`, `MYSQL`, `MARIADB`, `MSSQL`, `ORACLE`, `DB2`). PROCESS_APPLICATION only.

| Database | JDBC driver | Docker image | JDBC URL pattern |
|---|---|---|---|
| H2 | `com.h2database:h2` | none | default, no profile files, byte-identical to pre-feature baseline |
| PostgreSQL | `org.postgresql:postgresql` | `postgres:16-alpine` | `jdbc:postgresql://host:port/db` |
| MySQL | `com.mysql:mysql-connector-j` | `mysql:8.4` | `jdbc:mysql://host:port/db?sessionVariables=transaction_isolation=READ-COMMITTED&sendFractionalSeconds=false` |
| MariaDB | `org.mariadb.jdbc:mariadb-java-client` | `mariadb:11.4` | `jdbc:mariadb://host:port/db?sessionVariables=transaction_isolation=READ-COMMITTED` |
| MS SQL Server | `com.microsoft.sqlserver:mssql-jdbc` | `mcr.microsoft.com/mssql/server:2022-latest` | `jdbc:sqlserver://host:port;databaseName=operaton_test;encrypt=false`; needs generated `docker/mssql-init.sql` (create DB, `READ_COMMITTED_SNAPSHOT ON`) |
| Oracle | `com.oracle.database.jdbc:ojdbc11` | `gvenzl/oracle-free:latest` | `jdbc:oracle:thin:@//host:port/FREEPDB1`; ~2 min first-start; healthcheck `start_period` still open (OI-3) |
| DB2 | `com.ibm.db2:jcc` | `icr.io/db2_community/db2:latest` | `jdbc:db2://host:port/db`; `LICENSE=accept` pre-set in the generated Compose file; mandatory env vars for the image are the blocking open item (OI-2); may require a free IBM Cloud account for registry access |

Three-profile pattern for non-H2 databases: `application.properties` (sets `spring.profiles.active=local`), `application-local.properties` (localhost defaults), `application-docker.properties` (Compose `db` service hostname) — all `${KEY:default}` env-overridable. README documents the override list (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`) for manual (non-Compose) setup, plus `operaton.bpm.database.schema-update=true` so Operaton auto-creates its schema on first connect against an empty database.

**Docker Compose wiring:** the `app` service sets `SPRING_PROFILES_ACTIVE=docker` and declares `depends_on: db` with `condition: service_healthy`, so the app never starts against a database that isn't ready yet.

Generation test matrix (CI, NFR21 in prd.md): H2 × 3 build systems, PostgreSQL±compose, MySQL+compose, MSSQL+compose, Oracle+compose, DB2+compose.

## C. Examples Gallery — Manifest Schema Reference

`.operaton-starter.yml` / `.yaml`, `apiVersion: operaton-starter/v1`. Descriptors may live at any repository depth (discovered via one GitHub Trees API call per source, `?recursive=1`); a repo may have multiple descriptors; duplicate example `id`s within a repo are skipped (first-discovered wins) with a warning; if both `.yml` and `.yaml` exist in the same directory, `.yml` wins.

**Required per example:** `id`, `title`, `shortDescription` (≤200 chars), `path` (optional — defaults to the descriptor's own directory, resolved as `<descriptorDir>/<path>`).

**Optional per example:** `longDescription` (markdown), `authors`, `license`, `documentationUrl`, `demoVideoUrl`, `buildSystem`, `runtime`, `operatonVersion`, `javaVersion`, `requires`, `tags` (`{label, category}`), `integrations`, `bpmnConcepts`, `complexity`, `icon`, `screenshots`, `lastUpdated`.

**Optional manifest-root `repository` block:** `name`, `description`, `maintainer`.

Unknown fields are ignored (forward-compatible); unknown `apiVersion` major versions are refused with a warning.

**Download mechanism:** GitHub tarball at the pinned commit SHA, filtered to the example's resolved path, repacked as ZIP, streamed (no large in-memory buffering). Disk cache keyed by `(owner, repo, sha, exampleId)`, default max 512MB with LRU eviction, path/size overridable via `starter.examples.cache.dir` / `starter.examples.cache.maxSizeMb`.

**Endpoints (backend):** `GET /api/v1/examples/{owner}/{repo}/{exampleId}/download`; `POST /api/v1/examples/refresh` (re-fetches all sources, atomic snapshot swap, no auth in this version — OI-6); an actuator-style load-status endpoint per source; `/actuator/examples` exposes an in-process counter per `(sourceRepo, exampleId)` download (not persisted across restarts, no PII).

**Size limits (NFR11/NFR12 in prd.md):** manifests >256KB rejected; per-example uncompressed payload capped at 50MB (`starter.examples.maxDownloadSizeMb`), enforced during tarball filtering with streaming abort + partial-cache cleanup on breach (413 response).

**Repository-wide descriptor discovery (FR75, FR90, FR91):**
- Descriptors are found via one GitHub Git Trees API call per source (`GET /repos/{owner}/{repo}/git/trees/{sha}?recursive=1`), plus one fetch per discovered descriptor — total API calls per source refresh = 1 + N. For single-descriptor repos this is one extra round trip versus the original root-only design; accepted as worthwhile for the multi-descriptor and any-depth capability it buys.
- The existing per-call timeout and `SourceUnavailable` error handling apply to the tree-enumeration call exactly as they already did to manifest fetches — no separate mechanism was added.
- `path` field validation (when provided): no leading `/`, no `..` path segments, no null bytes, no empty string after trimming. Absent `path` defaults to `.` (FR75).
- A truncated tree response (`truncated: true`) logs a warning naming the source and processes only the visible descriptors (FR90); a non-recursive fallback strategy is explicitly deferred — expected example repos are small enough that this hasn't been a problem in practice.
- Per-descriptor outcomes are reported individually — descriptor path plus failure reason — inside that source's status detail (FR91), not just a single pass/fail per source.
- **GitHub rate-limit mitigation (NFR26):** async availability-validation results (HEAD-request checks) are cached rather than re-run on every load; this, together with the tree-scan cost above, keeps the gallery's own outbound GitHub usage well under GitHub's per-token rate limits even as the number of configured sources grows.

## D. Considered Alternatives / Rejected Options

- **DB selection:** blocking UI gates for "risky" databases (Oracle/DB2) considered and rejected — guidance lives in the generated README instead of blocking the form.
- **Examples Gallery:** periodic auto-refresh of manifests considered and rejected for this version — manifests refresh only at server startup and via the manual endpoint, to keep behavior predictable and avoid unbounded background GitHub API usage. UI-driven repository registration considered and deferred to a future version — the source list stays maintainer-curated via configuration.
- **Examples Gallery caching:** a risk note in the original PRD called caching "out of scope for v1" while a separate functional requirement in the same document specified a disk cache. `[ASSUMPTION: the functional requirement is authoritative and the risk note is the stale one, since risk notes describe residual concern rather than committed scope, and the disk-cache FR has concrete acceptance detail (key structure, size bound, eviction policy) that the risk note doesn't contradict on substance — only on the "in/out of scope" label. If this is wrong, FR82 in prd.md needs to be reopened as a design question rather than treated as settled.]` The shipped/specified behavior (FR82 in prd.md) is the disk cache.

## E. Excluded Source — UC-Enhancements PRD (historical pointer only)

`docs/bmad/planning-artifacts/_archived/prd-operaton-starter-uc-enhancements-2026-06-06/prd.md` specified process-start authorization, email-notification send events, a local Mailpit mail service, task-form data surfacing, and swimlane BPMN restructuring for the four built-in use cases. None of it is implemented in code as of this consolidation. Per the reconciliation in `prd.md` §9, it is not carried forward — left here only as a pointer for anyone auditing why a previously "final"-status PRD isn't reflected above.

## F. Strategic Context (from master PRD, condensed)

Carried here rather than in `prd.md` because it's positioning/rationale rather than a requirement — nothing here is testable as a capability.

**Competitive positioning:** operaton-starter is the first dedicated project initializer for the Operaton ecosystem, occupying the niche `start.spring.io` fills for Spring Boot and `code.quarkus.io` fills for Quarkus. No comparable Camunda-7-to-Operaton migration tooling exists at the time of writing.

**Validation approach (master PRD):** channel-consistency is checked by diffing output across all supported project-type × build-system combinations (15 at full Phase 3 scope) generated through each of the four channels — any byte-level divergence is a bug. The Camunda 7 migration path (Phase 2, deferred) targets an ≥80% first-try success rate on a representative migration corpus before being considered viable to ship.

**Risk register (master PRD, condensed — status as of this consolidation):**

| Risk | Mitigation |
|---|---|
| `migrate-from-camunda-recipe` (external dependency) going unmaintained | Fork under the Operaton org if it lapses |
| Archetype/generation engine latency risk against the ≤1s target | Benchmark in CI; pre-compile templates at deploy time |
| Community-contributed templates diverging from quality standards | Contribution checklist plus CI matrix validation on every template change |
| Spec drift between `openapi.yaml` and generated clients | Freeze the spec before regenerating clients; contract-check CI job |
| Template combinatorics growing faster than CI can validate | Start small (6 combinations at MVP), validate cost before expanding |
| Solo-maintainer resourcing constraint | Keep the CLI a thin generated wrapper rather than a maintained surface of its own |
| Low initial adoption | All channels (web, API, CLI, archetype) live from day one rather than phased |

**Persona detail not captured in the §2 table (prd.md):** Marcus's README troubleshooting notes are port-aware (flags the actual configured port, not a hardcoded default); Thomas's inline help copy disambiguates project types in plain language rather than jargon — both are UX-writing guidance for whoever implements FR39/FR55, not separately testable requirements.

**Built-in Use Cases — technical detail not reproduced in prd.md §5.8** (informational only, since the feature is sunset-planned per OI-1): UC-02 Loan Application uses a DMN decision table (`risk-assessment.dmn`, `FIRST` hit policy) for risk scoring; timer-dependent tests use `ClockUtil` to avoid real waiting; WireMock-backed use cases include a readiness check before asserting business logic. None of this changes as part of this consolidation — recorded here only so the detail isn't lost if/when someone picks up the sunset migration (OI-1).
