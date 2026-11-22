---
title: "operaton-starter — Consolidated Product PRD"
status: final
created: 2026-07-14
updated: 2026-07-14
project: operaton-starter
consolidates:
  - "docs/bmad/planning-artifacts/_archived/prd-operaton-starter-master-2026-03-27/prd.md (master product PRD, 2026-03-27, updated 2026-07-14)"
  - "docs/bmad/planning-artifacts/_archived/prd-operaton-starter-2026-06-06/prd.md (Database Selection)"
  - "docs/bmad/planning-artifacts/_archived/prd-operaton-starter-2026-06-08/prd.md (UI Enhancements)"
  - "docs/bmad/planning-artifacts/_archived/prd-operaton-starter-examples-gallery-2026-06-13/prd.md (Examples Gallery)"
  - "docs/bmad/planning-artifacts/_archived/prd-operaton-starter-2026-07-07/prd.md (Examples Gallery — Descriptor Discovery)"
excluded:
  - "docs/bmad/planning-artifacts/_archived/prd-operaton-starter-uc-enhancements-2026-06-06/prd.md — not implemented in code; treated as stale, retained only as historical record, not carried forward"
note: "All five consolidated source PRDs above (plus the pre-existing excluded one) were moved into _archived/ on 2026-07-14, after this document was finalized, to make this the single active PRD."
---

# Product Requirements Document — operaton-starter (Consolidated)

**Project:** operaton-starter
**Date:** 2026-07-14

> This document supersedes the five source PRDs listed in the frontmatter as the single point of reference for product scope. It reconciles five separately-authored documents into one coherent requirement set, fixes a duplicate FR ID, and retires struck scope. **Update (2026-07-14):** the built-in Use Cases feature is now fully removed from templates and code, resolving Open Item OI-1 — see §5.8 and §9.3.

## 1. Executive Summary

Operaton Starter is a stateless, open-source project generator hosted at `start.operaton.org` that bootstraps Operaton-based projects — process applications and process archives today, with engine plugins, connectors, and Camunda 7 migrations planned for later phases (§12) — as downloadable, ready-to-build, immediately runnable project archives. It is the first and only dedicated project initializer for the Operaton ecosystem, filling the gap that Spring Initializr fills for Spring Boot and code.quarkus.io fills for Quarkus.

Three first-class channels — a web UI, the `npx operaton-starter` CLI, and a single `curl` command — all invoke the same REST API (`POST /api/v1/generate`) and the same generation engine, guaranteeing no channel produces divergent output. The web UI serves two personas through a split landing experience: a direct configuration form for developers who already know what they want, and a visual gallery (project types, plus an Examples section sourced from external GitHub repositories) for developers who want to browse before committing. The tool is deployed as a single Spring Boot application on the `operaton.org` domain and distributed as a self-hostable Docker image, letting enterprise teams run a private instance behind their firewall with org-specific defaults.

Generated projects support Maven or Gradle (Groovy/Kotlin DSL), always target the current stable Operaton release, and include a personalized README. Since June 2026, generated Process Application projects can also select a target database (PostgreSQL, MySQL, MariaDB, MS SQL Server, Oracle, DB2, or the H2 default) with matching Docker Compose services and connection profiles. Optional Extras — Dependency Updates, Docker Compose, GitHub Actions CI/CD — remain off by default. Projects are identity-aware: Group ID, Artifact ID, and project name propagate into BPMN process IDs, Java packages, and Spring `application.name`. No authentication, no user profiles — stateless by design.

## 2. Personas

| Persona | Profile | Need |
|---|---|---|
| **Marcus — Practitioner** | Senior Java developer, Operaton-experienced | Zero-friction path from decision to running project |
| **Thomas — BPMN Newcomer** | Spring Boot developer new to BPM tooling | Gallery discovery, inline guidance disambiguating project types |
| **Klaus — Self-Hosted Admin** | Runs the Docker image on-premises under compliance constraints | No external dependencies, full env-var configurability |

Today's Explorer is tomorrow's Practitioner — the quality of the first session directly determines ecosystem retention.

**Future persona (not yet served by any FR in §5):** **Elena — Camunda Migrator**, a process architect evaluating Operaton as a Camunda 7 migration path, needing confidence and a structured migration plan. Elena's need maps entirely to the Camunda 7 Migration project type in Phase 2 (§12) — she is recorded here so the roadmap item retains its "why," not because current scope addresses her.

**Priya — Platform Engineer** (REST API consumer integrating into an internal developer portal such as Backstage) was considered as a fifth persona but folded out: nothing in §5.4 currently distinguishes portal/programmatic API consumption from any other REST API caller. If a Backstage-specific integration becomes real scope, reintroduce Priya alongside the FR(s) that serve her (e.g. a stability guarantee on the metadata schema for automated consumers).

### 2.1 User Journeys

Brief, capability-checking walkthroughs for the two personas whose journeys span multiple FRs — not full narrative UJs, since this PRD is predominantly a capability spec (§5) rather than UX-narrative-shaped.

- **UJ-1 (Thomas, Explorer path):** Lands on the gallery (FR29), browses project types, opens the inline explanation to tell Process Application from Process Archive apart (FR33), picks a project-type card (FR32 — type locked read-only), watches the live file-tree preview update as he reviews defaults (FR36–FR38), and downloads. Checks that FR29→FR33→FR32→FR36 actually compose into one continuous flow with no dead end.
- **UJ-2 (Marcus, Practitioner path):** Clicks "Configure Now" straight from the landing hero (FR31), gets the form with Process Application pre-selected but still editable, sets Group ID/Artifact ID/version (FR9, FR12), picks Maven, enables Docker Compose with PostgreSQL (FR15, FR19, FR22), downloads, and follows the generated README's `chmod +x` instruction to run it in under 30 seconds end to end (FR55, NFR3). Checks that the "recommend but don't force" Compose nudge (FR22) doesn't add friction to this path.
- **UJ-3 (Klaus, Self-hosted admin):** Pulls the Docker image (FR66), sets `STARTER_DEFAULTS_GROUP_ID` and `STARTER_EXAMPLES_REPOSITORIES` via environment variables with no config file (FR68, NFR24), confirms the health check endpoint (FR69) before putting the instance behind a load balancer, and never sees an outbound call to a service he hasn't explicitly configured (NFR6).

## 3. Product Architecture (Decisions in Force)

- **Channels:** web UI (`start.operaton.org`), REST API (`POST /api/v1/generate`), CLI (`npx operaton-starter`), Maven archetype (`mvn archetype:generate`) — all invoke one shared generation engine; no per-channel generation logic.
- **Deployment:** single Spring Boot application; public hosted instance plus a self-hostable Docker image (one port, UI + API + engine bundled).
- **Stateless by design:** no authentication, no user profiles, no database dependency for the starter itself; all request state is ephemeral; horizontally scalable with no sticky sessions.
- **Build systems:** Maven; Gradle (Groovy DSL); Gradle (Kotlin DSL) — selected in two steps (build system, then DSL if Gradle).
- **Version policy:** always targets the current stable Operaton release; no version picker on the public instance (self-hosted instances may pin a version via env var).
- **Identity propagation:** Group ID, Artifact ID, and project name propagate consistently into Java packages, BPMN process IDs, `processes.xml`, and Spring `application.name`.
- **Spec-first API:** `openapi.yaml` is the single source of truth; server stubs and client code are generated from it — never hand-edited.
- **Metadata as source of truth:** `GET /api/v1/metadata` drives every configuration option and template manifest shown by the web UI and CLI; no hardcoded option lists.
- **Extras (opt-in, all off by default):** Dependency Updates (Dependabot or Renovate), Docker Compose, GitHub Actions CI/CD skeleton.
- **Rate limiting:** per-IP token bucket (default 60 requests/minute, configurable via `RATE_LIMIT_REQUESTS_PER_MINUTE`); breach returns HTTP 429 with `Retry-After`.

Implementation-level mechanism detail (JDBC connection strings, GitHub API call patterns, cache directory layout, manifest schema) lives in `addendum.md`, not here.

## 4. Success Metrics

| Outcome | Target |
|---|---|
| UI landing → ZIP download | ≤ 30 seconds |
| REST API generation time | ≤ 1 second (up to 10 concurrent requests) |
| Generated project compile rate | 100% across all supported combinations |
| Generated project CI pass rate | 100% |
| Version update lag after an Operaton release | ≤ 24 hours |
| Public instance availability | 99.9% (rolling 30-day window) |
| Non-H2 database adoption (of Process Application generations) | ≥ 30% within 90 days of the database-selection feature shipping |
| Datasource misconfiguration bug reports | Zero, across all supported database options |
| Community-contributed Examples Gallery entries | ≥ 3 new examples within 90 days; ≥ 1 third-party (non-`operaton/operaton-examples`) source repository registered within 6 months |

**Counter-metrics to watch:** combinatorial explosion of project-type × build-system × database × extras combinations degrading CI feedback speed or generation reliability; Examples Gallery growth outpacing the async-validation and cache-eviction budget (NFR, §7); rate-limit tuned so aggressively it blocks legitimate CI/scripted use of the public instance.

## 5. Features and Functional Requirements

FR IDs are renumbered sequentially and are stable going forward from this document's `created` date. A mapping back to each source PRD's original FR IDs is kept in `addendum.md §A` for audit purposes.

### 5.1 Core Generation Engine

- **FR1** The system generates a project archive for any supported project type × build system combination via one shared generation engine invoked identically by every channel.
- **FR2** Generated projects compile and pass tests unmodified — complete working examples, not minimal scaffolds; "complete" is operationally defined by the CI validation in NFR21 (every supported combination compiles, tests pass, app starts).
- **FR3** Generation always targets the current stable Operaton release on the public instance.
- **FR4** Generated files consistently reflect the developer's Group ID, Artifact ID, and project name.
- **FR5** Generated BPMN files are graphically valid, with complete shape/edge layout data.
- **FR6** Process Archive projects generate a `processes.xml` pre-configured for the selected deployment target.
- **FR7** Process Archive projects generate target-platform-appropriate packaging (WAR/JAR).
- **FR8** CLI client code is generated from the OpenAPI spec — no hand-written client code.

### 5.2 Project Configuration

**Identity & coordinates**
- **FR9** The developer specifies Group ID, Artifact ID, and project name; both Group ID and Artifact ID remain freely editable.
- **FR10** The default Group ID for example/gallery-originated projects is `org.operaton.example`.
- **FR11** Default Artifact ID and project name do not contain the word "example"; naming follows the selected project type (e.g. `my-process-app`).
- **FR12** The configuration form includes a version field, defaulting to `1.0.0-SNAPSHOT`, accepting any non-empty, whitespace-free Maven-format version string; invalid input shows an inline error and blocks generation.

**Build system & platform**
- **FR13** Build system is selected in two steps: Maven vs. Gradle, then Groovy/Kotlin DSL if Gradle.
- **FR14** For Process Archive projects, the developer selects a target platform (MVP: Tomcat, Wildfly; extensible), determining the artifact type and descriptor.

**Database selection** *(added June 2026, PROCESS_APPLICATION only)*
- **FR15** The developer selects a target datasource for generated Process Application projects: H2 (default), PostgreSQL, MySQL, MariaDB, MS SQL Server, Oracle, or DB2.
- **FR16** The generated build file includes the correct JDBC driver for the selected database at runtime scope, kept current via the repository's own dependency-update automation.
- **FR17** Selecting H2 produces output identical to the pre-database-selection baseline — no profile files, no behavior change.
- **FR18** Selecting a non-H2 database produces environment-overridable connection configuration for both local and Docker execution.
- **FR19** When Docker Compose is enabled and a non-H2 database is selected, the generated Compose file provisions a matching database service with a healthcheck, and the app service waits on it.
- **FR20** The generated README documents database setup for all three cases: H2 default, non-H2 with Docker Compose, and non-H2 without Docker Compose (manual install instructions).
- **FR21** All generation channels (REST API, CLI) expose database selection; the CLI additionally prompts for it interactively between project-type and build-system questions.
- **FR22** The web UI's database selector lives in the configuration form's Infrastructure section, next to the Docker Compose toggle. Choosing a non-H2 database visually recommends (but does not force) enabling Docker Compose.
- ~~**FR23**~~ **Retired (2026-07-14).** Formerly: use case examples reached via the gallery defaulted to PostgreSQL with Docker Compose enabled. Moot now that Use Cases are removed (§5.8) — there is no longer an in-app generation path this default could apply to; Examples Gallery downloads are static repository content, not run through `POST /api/v1/generate`.

**Database selection non-goals:** schema migration tooling (Flyway/Liquibase) in generated projects; database selection for Process Archive or DMN Project types; guaranteeing that every Docker-image/Operaton-version combination is covered by Operaton's own test suite (registry access and image currency are the user's responsibility, documented in the generated README).

**Extras**
- **FR24** Dependency Updates is opt-in; when enabled, the developer selects Dependabot or Renovate.
- **FR25** Docker Compose is opt-in, offered only for project types that support containerized embedded deployment (hidden for Process Archive).
- **FR26** GitHub Actions CI/CD skeleton is opt-in.
- **FR27** All Extras are unchecked by default.
- **FR28** A configuration can be shared as a URL that restores/pre-fills the form.

### 5.3 Web UI

**Landing & navigation**
- **FR29** A project type is selected from the gallery/landing page before the configuration form is reached — a prerequisite, not a form field.
- **FR30** The landing page hero offers, in order: a primary "Configure Now →" call-to-action and a secondary, outlined-style "Project Types ↓" link. *(Previously also listed a "Browse Use Cases ↓" link alongside "Project Types ↓"; removed 2026-07-14 along with the Use Cases feature — see §5.8.)*
- **FR31** "Configure Now" navigates directly to the configuration form with `PROCESS_APPLICATION` pre-selected and the project-type field left editable; if `PROCESS_APPLICATION` is unavailable in the metadata response, the form falls back to the first available project type.
- **FR32** Reaching the configuration form from a project-type card (i.e. with an explicit type already chosen) keeps the project-type field read-only, as before.
- **FR33** The developer can access an explanation distinguishing the available project types.

**Configuration form & preview**
- **FR34** The full configure-and-download flow is completable without a mouse.
- **FR35** Configuration options are conditionally rendered per project type; inapplicable options are hidden entirely, not disabled.
- **FR36** A live, interactive file tree preview updates as configuration changes, rendered client-side from template manifests in the metadata response — no per-change server round trip.
- **FR37** Selecting a file in the preview shows its content in an adjacent pane, with template placeholders substituted client-side using live form values.
- **FR38** The content pane re-renders reactively on any configuration change while a file is open, and clears immediately if the open file no longer applies (e.g. switching Maven → Gradle removes `pom.xml`).
- **FR39** Inline contextual help is available for any configuration option without leaving the page.
- **FR40** All web UI configuration options and gallery content are populated from the REST API metadata endpoint.
- **FR41** The web UI serves a favicon derived from the Operaton logo (no 404 on `/favicon.ico`).

**Gallery**
- **FR42** The gallery lists project types first, followed by the Examples section (see §5.9). *(Previously also listed a curated use cases section, between project types and Examples; removed 2026-07-14 — see §5.8.)*
- **FR43** Tag chips across gallery cards (project types, Examples) are color-coded by category — BPMN concept, technology, platform, or standard — with an unrecognized/missing category rendering as a neutral grey chip; colors meet WCAG AA contrast.
- **FR44** A search box and filter chips narrow the Examples section by title, description, tags, runtime, build system, complexity, and integrations.
- **FR45** An empty Examples section (no sources loaded) explains how to register a repository and links to the manifest-format documentation.

**Footer**
- **FR46** The footer displays `operaton-starter X.Y.Z` (pre-release suffixes such as `-SNAPSHOT` stripped) alongside the Apache 2.0 license and operaton.org links; if no version is available at build time, the footer omits the version string rather than showing a placeholder or `undefined`. Version injection requires no manual step in the standard Maven build (`mvn package`, `mvn spring-boot:run`) or in `vite dev`.

**UI-enhancement non-goals:** changes to generated project template content or structure; new project types or use cases; an accessibility audit beyond WCAG AA specifically for tag chip colors; dark mode; validation of Maven coordinate fields beyond the version field (Group ID/Artifact ID format).

### 5.4 REST API

- **FR47** `POST /api/v1/generate` generates and returns a ZIP archive.
- **FR48** `GET /api/v1/generate` supports the same generation as a query-parameter mode, for IDE deep-links and shareable URLs.
- **FR49** `GET /api/v1/metadata` returns all configuration options, template manifests, and Examples Gallery entries.
- **FR50** The OpenAPI spec and an interactive API docs UI are available at `/api/v1/docs`.
- **FR51** The API enforces a per-IP rate limit and returns a structured error on breach.

### 5.5 CLI

- **FR52** `npx operaton-starter` generates and downloads a project with all options available as flags.
- **FR53** The CLI writes raw archive bytes to stdout when stdout is piped (scriptable mode).
- **FR54** The CLI can extract the archive into a specified directory instead of writing raw bytes.

### 5.6 Generated Project Quality

- **FR55** The generated README includes project-specific next-step instructions reflecting the actual configuration (Cockpit URL, ports, Docker Compose steps), including a `chmod +x` instruction for the build tool wrapper on Mac/Linux, and an embedded or linked image of the BPMN model.
- **FR56** Every generated project includes the appropriate build tool wrapper so it builds without a globally installed Maven or Gradle.
- **FR57** Enabling Dependency Updates yields a configured Dependabot or Renovate file; omitted otherwise.
- **FR58** Generated Process Application projects include a GitHub Actions CI/CD workflow that passes on first push, when enabled.
- **FR59** Enabling Docker Compose yields a `docker-compose.yml` and a multi-stage `Dockerfile`.
- **FR60** Generated projects include complete, runnable delegate implementations wired to BPMN service tasks, verified by a JUnit test that deploys and executes the full process end-to-end.
- **FR61** Generated Spring Boot Process Application projects display an Operaton banner (ASCII logo plus resolved versions) on startup.
- **FR62** Generated projects separate domain logic, process resources (BPMN/DMN), configuration, and tests into distinct packages/directories, per the same module-boundary convention CLAUDE.md documents for this repository; no logic lives in the default/root package.
- **FR63** Every supported combination compiles, passes tests, and starts on first run, verified by CI on every template change.
- **FR64** Generated Process Application projects seed an Operaton admin user at startup if one doesn't already exist, so Cockpit always has valid credentials on first run.
- **FR65** Generated projects include a "Bootstrap Data" README section explaining how seed data (`data.sql`) is applied and re-applied.

### 5.7 Self-Hosting & Operations

- **FR66** Operators deploy via a single Docker image bundling the web UI, REST API, and generation engine on one port, with no external service dependency at startup.
- **FR67** The repository contains a `Dockerfile` for building the application image.
- **FR68** Self-hosted defaults (default Group ID, Maven registry URL, Operaton version) are configurable via environment variables — see CLAUDE.md's environment variable table for the current set (`STARTER_DEFAULTS_GROUP_ID`, `STARTER_DEFAULTS_OPERATON_VERSION`, `STARTER_CORS_ALLOWED_ORIGINS`, `STARTER_EXAMPLES_REPOSITORIES`, `RATE_LIMIT_REQUESTS_PER_MINUTE`).
- **FR69** The running instance exposes a health check endpoint.

### 5.8 Use Cases — Removed

> **Status: removed (2026-07-14).** The built-in Use Cases feature (four in-app-generated example processes: Leave Request, Loan Application, Incident Management, Order Fulfillment) has been removed from operaton-starter's templates and generation engine. Use cases are now maintained exclusively in external repositories (`operaton/operaton-examples`) and surfaced through the Examples Gallery (§5.9) like any other community-contributed example, with no in-app distinction between "a use case" and "an example" anymore. This resolves Open Item OI-1 (§8) — see §9.3 for the decision record. No functional requirements remain active against this feature.

**Retired FRs (no longer implemented; kept for traceability only — see addendum §A):**
- ~~FR70~~ — in-app use-case self-containment (`docker compose up -d` + standard run command, no manual config)
- ~~FR71~~ — per-use-case seeded roles/groups via `data.sql`
- ~~FR72~~ — per-use-case PostgreSQL-default/H2-fallback Docker Compose profile switching
- ~~FR73~~ — cross-channel use-case discoverability and `useCaseId`-driven generation via `POST /api/v1/generate`
- ~~FR89~~ (§5.11) — per-use-case live template-manifest preview

Anyone wanting this functionality for a specific example should propose it against the Examples Gallery's manifest schema (§5.9, `docs/examples-repository-format.md`) instead — e.g. a manifest field indicating a recommended default database, rather than a generation-engine special case.

### 5.9 Examples Gallery — Remote Example Repositories

**Manifest & registration**
- **FR74** Example repositories publish an `.operaton-starter.yml` (or `.yaml`) manifest describing one or more examples (title, description, build system, runtime, integrations, tags, complexity, and similar descriptive metadata); the full schema is documented separately (`docs/examples-repository-format.md`) and is the source of truth, not duplicated here.
- **FR75** Manifests may live at any depth in the repository tree, not only at the root, and a repository may publish multiple manifests; each example's content directory defaults to the manifest's own directory when no explicit path is given.
- **FR76** The starter maintains a maintainer-controlled list of source repositories (configuration, not UI, in this version), preconfigured with `operaton/operaton-examples`, overridable via the `STARTER_EXAMPLES_REPOSITORIES` environment variable.
- **FR77** A single source's manifest failure (missing, malformed, unreachable) never blocks startup or other sources; it is skipped with a logged reason, and a manual refresh operation can retry all sources.
- **FR78** Manifest and example content are resolved against a pinned commit of the source repository at each load, so mid-session repository changes never affect a user's current gallery view.

**Discovery & download**
- **FR79** `GET /api/v1/metadata` lists Examples Gallery entries with enough computed fields (source repository, resolved commit, download availability) for the UI to render and gate the download action. *(Previously described as "parallel to the use-case list" — the use-case list no longer exists, see §5.8.)*
- **FR80** Each example can be downloaded as a ZIP of its content directory with one click, mirroring the download-a-generated-project experience.
- **FR81** Example availability is validated asynchronously after each manifest load; a card whose target no longer exists shows as unavailable with a link to the source repository, rather than failing at download time.
- **FR82** Downloaded ZIPs are served from a bounded, evictable cache so repeat downloads of the same example/commit do not require a fresh fetch from GitHub.

**Non-goals (this version):** user-configurable repository list in the UI; authentication for private repositories; server-side validation/execution of example projects; per-example version pinning by the end user.

### 5.10 Documentation

- **FR83** `docs/examples-repository-format.md` documents the manifest schema, an annotated example, and instructions for getting a repository registered; the gallery's Examples section and the main README link to it.
- **FR84** Every monorepo submodule (per CLAUDE.md's module list) has its own README sufficient to build and run it standalone.

### 5.11 Supplementary Requirements (folded in during input reconciliation)

These four items surfaced when each source PRD was diffed against the first consolidated draft; they are genuine requirements that were missing, not new scope. Numbered to continue the sequence without disturbing FR1–FR84 above.

- ~~**FR89**~~ **Retired (2026-07-14).** Formerly: each use case's live file-tree preview (FR36) reflected that use case's own template manifest. Moot now that Use Cases are removed (§5.8).
- **FR90** If the Examples Gallery's repository-tree scan is truncated by the GitHub API, the affected source logs a warning identifying itself and processes whatever descriptors were visible rather than being dropped or failing outright.
- **FR91** A source's per-descriptor load failures are exposed individually (descriptor path plus failure reason) in that source's status detail, not just as a single pass/fail per source.
- **FR92** The repository's own dependency-update automation (Dependabot/Renovate on the operaton-starter repo itself, not on generated projects) proposes Operaton version bumps as they become available.

## 6. Release & Distribution

- **FR85** Releases are cut via GitHub Actions + JReleaser, producing a GitHub Release, a changelog from conventional commits, and coordinated publishing across all targets.
- **FR86** The Docker image is published to Docker Hub as `operaton/operaton-starter` on every release, tagged with both the semver version and `latest`.
- **FR87** Maven artifacts are published to Maven Central via Sonatype OSSRH, coordinated by JReleaser.
- **FR88** Release documentation specifies every required CI secret (Docker Hub, Maven Central/Sonatype, GitHub token for JReleaser).

## 7. Non-Functional Requirements

**Performance**
- **NFR1** `POST /api/v1/generate` returns a complete ZIP within 1 second under up to 10 concurrent requests.
- **NFR2** Web UI live preview updates within 200ms of any configuration change.
- **NFR3** End-to-end flow (landing to ZIP download) completes within 30 seconds.
- **NFR4** Examples Gallery startup overhead stays within 3 seconds for up to 10 configured sources, fetched in parallel; a 5-second per-source timeout bounds worst-case delay so one slow source cannot stall the others.

**Availability & Resilience**
- **NFR5** The public instance targets 99.9% uptime over a rolling 30-day window.
- **NFR6** The self-hosted Docker image starts with no external network calls and no runtime database dependency for the starter itself; failure of an external service (e.g. an unreachable example repository) never blocks generation.
- **NFR7** A single Examples Gallery source failing never blocks startup or the availability of other sources.

**Security**
- **NFR8** All traffic to the public instance is served over HTTPS.
- **NFR9** No user-identifying data is persisted; only transient per-IP rate-limit counters are held, discarded after the window.
- **NFR10** Rate-limit breaches return HTTP 429 with a `Retry-After` header.
- **NFR11** Examples Gallery manifest parsing is safe against untrusted YAML content (no arbitrary object instantiation) and rejects oversized manifests or example payloads.
- **NFR12** The starter never executes or evaluates content from a downloaded example; it only repackages files.

**Scalability**
- **NFR13** Instances are horizontally scalable and interchangeable — no sticky sessions, no instance-local state.

**Accessibility**
- **NFR14** WCAG 2.1 AA conformance, validated by automated tooling (axe-core) in CI and manual keyboard testing before release.
- **NFR15** All web UI functionality is operable via keyboard with visible focus indicators.

**Compatibility**
- **NFR16** Generated Process Application projects target Java 21+ and the Spring Boot version from the current Operaton BOM.
- **NFR17** Generated Gradle projects target Gradle 8+.
- **NFR18** Browser support: latest two major versions of Chrome, Firefox, Safari, Edge.
- **NFR19** Web UI visual design stays consistent with the `operaton.org` / `docs.operaton.org` design system.
- **NFR20** Adding a database option or an Examples Gallery source is additive and backward-compatible — existing callers omitting the new fields see unchanged output/behavior.

**Correctness**
- **NFR21** All supported project-type × build-system × database combinations are validated in CI on every template change; failures block merge.
- **NFR22** On template-modifying PRs, CI identifies and validates only the affected combinations, to keep feedback fast as the combination count grows.

**Maintainability & Operability**
- **NFR23** The service emits structured JSON logs (one JSON object per line, standard fields: timestamp, level, logger, message) parseable by common log aggregators (e.g. Logstash, Fluentd, CloudWatch Logs) without a custom parser.
- **NFR24** The Docker image is configurable entirely via environment variables; no file-based runtime configuration is required.
- **NFR25** Manifest load results and example download attempts are logged with enough structured detail (source, example ID, outcome, duration) for operators to diagnose gallery issues without reading application code, and per-`(source, example)` download counts are exposed for operators without persisting them across restarts.
- **NFR26** Example-availability validation results are cached rather than re-checked on every load, so the Examples Gallery's own outbound GitHub API usage (tree scans, validation checks) stays well clear of GitHub's rate limits independent of the starter's own inbound per-IP limiting (NFR10).

## 8. Open Items

| ID | Item | Owner | Status | Condition to resolve |
|---|---|---|---|---|
| OI-1 | Use Cases sunset date and migration plan to `operaton/operaton-examples` | Product | **Resolved 2026-07-14** — feature removed, see §5.8 and §9.3 | — |
| OI-2 | DB2 (`icr.io/db2_community/db2`) mandatory environment variables for `docker-compose.yml`/`application-docker.properties` | Engineering | **Blocker** for the DB2 database option specifically | Must resolve before the DB2 template is authored |
| OI-3 | Oracle Docker Compose healthcheck `start_period` tuning (first-start ~2 min) | Engineering | Open, non-blocking | Before finalizing the Oracle template |
| OI-4 | Gradle JDBC driver version management — Spring Boot BOM vs. explicit pin | Engineering | Open, non-blocking | Before implementation of remaining database options |
| OI-5 | "One-dependency constraint lifted" (master PRD frontmatter, 2026-07-14 updateNotes) has no corresponding explanation anywhere in any source PRD body | Product | Open — needs owner to explain or retire the note | Before next PRD update cycle |
| OI-6 | Manual refresh endpoint for Examples Gallery sources (`POST /api/v1/examples/refresh`) currently requires no authentication | Engineering | Open, accepted risk for now | Revisit if the starter becomes commonly exposed to untrusted networks |

## 9. Reconciliation Notes

This consolidation surfaced two conflicts between source documents that could not be resolved from the documents alone; both were decided with the product owner during this consolidation pass and are recorded here for audit:

1. **Use Cases vs. Examples Gallery.** The Examples Gallery PRD (2026-06-13) states built-in Use Cases were "migrated" and "replaced" by the gallery. A codebase check during this consolidation found both features still fully present and coexisting (`useCaseExamples` and `examples` are both served by `GET /api/v1/metadata`; `starter-templates` still ships all four use-case templates). Decision at the time: treat Use Cases as deprecated and sunset-planned (§5.8), Examples Gallery as their intended replacement, without asserting a completion that hadn't happened in code yet. Tracked as OI-1. **Superseded by §9.3 below** — the sunset has since been executed.
2. **UC-enhancements PRD.** The archived PRD (2026-06-06/07) specifies authorization, email-notification, Mailpit, and task-form-data enhancements to the four built-in use cases. A codebase check found none of it implemented. Given the Use Cases sunset decision above, the product owner chose to drop this PRD from the consolidated requirement set entirely rather than carry it forward as backlog against a feature being phased out. The original document remains at `docs/bmad/planning-artifacts/_archived/prd-operaton-starter-uc-enhancements-2026-06-06/prd.md` as historical record only. Now doubly moot: even if reintroduced, its target (the in-app Use Cases feature) no longer exists.
3. **Use Cases removal (resolves OI-1).** On 2026-07-14, following this consolidation, the product owner directed removal of the built-in Use Cases feature from templates and code entirely — use cases are now maintained only in external repositories and surfaced via the Examples Gallery, with no special-cased generation path. §5.8 updated from "deprecated, sunset-planned" to "removed." Retired: FR23, FR70–FR73, FR89. Edited to drop use-case-specific wording: FR11, FR30, FR32, FR42, FR43, FR49, FR79. See addendum §A for the retirement mapping and the corresponding code-removal work (Java `UseCaseExample`/`buildUseCaseExamples()`, `starter-templates` use-case resources/JTE templates, web UI use-case gallery components, OpenAPI schema fields) tracked outside this PRD as an implementation task.

## 10. Glossary

- **Use case** — historical term for the four built-in, in-app-generated example processes (Leave Request, Loan Application, Incident Management, Order Fulfillment). **Removed as of 2026-07-14** (§5.8); no longer a distinct concept from an Examples Gallery entry.
- **Example** — an entry in the Examples Gallery, sourced from an external GitHub repository's manifest (§5.9). The current and intended long-term mechanism for showcasing working Operaton projects.
- **Manifest** and **descriptor** — the same artifact, the `.operaton-starter.yml`/`.yaml` file that declares one or more Examples. `prd.md` consistently says "manifest"; `addendum.md` §C introduces "descriptor" when discussing per-directory discovery, because a repository can have several of them at different depths — at that point "descriptor" emphasizes the individual file, while "manifest" emphasizes its content/schema. They are not different things.
- **Extras** — the opt-in, off-by-default generation add-ons: Dependency Updates, Docker Compose, GitHub Actions CI/CD (§3, §5.2).
- **Source** (Examples Gallery context) — one configured `owner/repo[@ref]` entry that may itself contain multiple descriptors/manifests and therefore multiple Examples.

## 11. Assumptions Index

Every `[ASSUMPTION: …]` tag inline in this document or `addendum.md`, gathered for quick review:

1. **`addendum.md` §D, Examples Gallery caching** — that the disk-cache functional requirement (not the "out of scope" risk note) is the authoritative statement of intended behavior, where the two source-document statements conflicted.

*(A former entry 1 here — the §5.8 Use Cases status assumption — was resolved by explicit product-owner decision on 2026-07-14 and removed from this index; the decision itself is recorded in §9.3, not as a standing assumption.)*

## 12. Deferred / Roadmap (from master PRD, unchanged)

**Phase 2 (Growth):** Camunda 7 Migration project type; opt-in anonymous telemetry and usage stats; full interactive CLI; "What's New" release banner.
**Phase 3 (Expansion):** Engine Plugin project type; Backstage Software Template plugin; multi-module project generation; tested stack snapshots.
**Phase 4 (Advanced):** Connector project type; community configuration gallery (needs a persistent-storage design review — breaks the stateless principle); Git repository push (needs an OAuth/design review — breaks the stateless principle); formula/recipe system; upgrade-aware Maven plugin.

**Explicitly out of scope, not reconsidered by this consolidation:** the MCP integration channel and `operaton-starter-mcp` npm package (removed from scope entirely, per the master PRD's 2026-07-14 update — see git history for the removal commit).

**Deferred example idea, with rationale:** a `document-approval` example (multi-level review plus a MinIO file archive) was considered and deferred — file storage is infrastructure, not process logic, and the Explorer audience should learn Operaton, not MinIO. Now that Use Cases are removed (§5.8, §9.3), any future version of this idea targets the Examples Gallery exclusively — there is no other path left to consider.

---
_Technical mechanism detail (manifest schema, per-database JDBC/Docker image reference table, GitHub API interaction pattern, cache layout, FR traceability to source documents) is kept in `addendum.md`._
