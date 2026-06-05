---
stepsCompleted: ['step-01-init', 'step-02-discovery', 'step-02b-vision', 'step-02c-executive-summary', 'step-03-success', 'step-04-journeys', 'step-05-domain', 'step-06-innovation', 'step-07-project-type', 'step-08-scoping', 'step-09-functional', 'step-10-nonfunctional', 'step-11-polish', 'step-12-complete']
workflowStatus: complete
completedAt: '2026-03-27'
updatedAt: '2026-06-05'
updateNotes: 'Postgres as default datasource for all use cases; H2 fallback profile; admin user auto-creation; chmod+x instructions; BPMN images in README; bootstrap data docs; one-dependency constraint lifted'
inputDocuments: ['_bmad-output/brainstorming/brainstorming-session-2026-03-25-1.md']
workflowType: 'prd'
classification:
  projectType: 'hybrid: web_app (primary) + api_backend + cli_tool'
  operatingModel: 'open-source hosted tool'
  domain: 'Developer Tooling / Open Source Ecosystem'
  complexity: 'medium'
  projectContext: 'greenfield'
  cli: 'npx operaton-starter'
---

# Product Requirements Document - operaton-starter

**Author:** Karsten
**Date:** 2026-03-27

## Executive Summary

Operaton Starter is a stateless, open-source project generator hosted at `start.operaton.org` that bootstraps Operaton-based projects — process applications, process archives, engine plugins, connectors, and Camunda 7 migrations — as downloadable, ready-to-build, immediately runnable project archives. It is the first and only dedicated project initializer for the Operaton ecosystem, filling the gap that Spring Initializr fills for Spring Boot and code.quarkus.io fills for Quarkus.

The tool targets two developer personas: **Practitioners** (Operaton-familiar developers who know what they want to build and demand zero friction) and **Explorers** (BPMN-literate developers new to Operaton, including Camunda 7 migrators, who benefit from guided discovery). Today's Explorer is tomorrow's Practitioner — the quality of the first session directly determines ecosystem retention. Access is available through three first-class channels: a web UI at `start.operaton.org`, the `npx operaton-starter` CLI, and a single `curl` command — all backed by the same REST API (`POST /api/v1/generate`). The web UI serves both personas through a split landing page — a direct configuration form for Practitioners and a visual project gallery for Explorers. An MCP module exposes the same generation API to AI-assisted development workflows. The tool is deployed as a Spring Boot application on the `operaton.org` domain and distributed as a self-hostable Docker image, allowing enterprise teams to run a private instance behind their firewall with org-specific defaults.

Generated projects support Maven or Gradle (Groovy/Kotlin DSL), always target the current stable Operaton release (no version picker), and include a personalized README with next-step instructions. Optional Extras — Dependency Updates (Dependabot or Renovate), Docker Compose, GitHub Actions CI/CD skeleton — are all off by default and must be explicitly enabled. Projects are identity-aware — Group ID, Artifact ID, and project name propagate into BPMN process IDs, Java packages, and Spring `application.name`. Generation templates are open-source and community-forkable. No authentication, no user profiles — stateless by design.

### What Makes This Special

No Operaton project initializer exists. For a growing ecosystem onboarding both net-new developers and Camunda 7 migrators, this gap is the primary problem this tool solves — and solving it is sufficient justification to build it.

The killer feature for adoption is **first-class Camunda 7 migration support**: a dedicated project type that integrates with the existing `operaton/migrate-from-camunda-recipe` OpenRewrite tooling, generating a migration scaffold with dependency substitutions and a structured checklist of manual migration steps. This makes migration a first-class path, not an afterthought.

The forward-looking differentiator is the **MCP module** — it exposes the generation REST API as an MCP tool, making Operaton Starter natively callable from AI assistants (Claude, GitHub Copilot, Cursor) during development conversations. No comparable BPM project initializer offers this. The baseline experience closes with a single `curl` command that generates a valid project.

## Project Classification

| Dimension | Value |
|-----------|-------|
| **Project Type** | Hybrid: Web App (primary) + REST API + CLI Tool |
| **Operating Model** | Open-source hosted tool |
| **Domain** | Developer Tooling / Open Source Ecosystem |
| **Complexity** | Medium |
| **Project Context** | Greenfield |
| **CLI** | `npx operaton-starter` |
| **Hosted At** | `start.operaton.org` |

---

## Success Criteria

### User Success

- **Practitioner flow:** A Practitioner arriving at `start.operaton.org` with a clear project type in mind completes configuration and downloads a ZIP within 30 seconds.
- **Explorer flow:** An Explorer using the gallery selects a project type, reviews the live preview, and downloads a project without needing to consult external documentation.
- **First-run success:** Every generated project compiles, tests pass, and the application starts with `./mvnw spring-boot:run` (or `./gradlew bootRun`) without any manual modification. This is a hard quality guarantee, not a target.
- **CI success:** Generated projects with the GitHub Actions skeleton pass CI on first push — green checkmark before the developer writes a single line of their own code.
- **curl success:** A developer who has never visited `start.operaton.org` can generate a valid project using only a documented `curl` command copied from the README or docs.

### Business Success

Operaton Starter is an open-source infrastructure tool with no authentication or default telemetry. Success is measured qualitatively:

- `start.operaton.org` is the canonical entry point referenced in Operaton's official "Get Started" documentation.
- Community adoption is evidenced by GitHub stars, forum references, and downstream project scaffolding visible in public repositories.
- The tool is maintained in sync with every Operaton release — no lag between an Operaton version shipping and the starter generating against it.
- Enterprise adoption is evidenced by self-hosted Docker image pull counts and community-reported deployments.

### Technical Success

- **Generation speed:** REST API response for `POST /api/v1/generate` ≤ 1 second under normal load.
- **Availability:** The public instance at `start.operaton.org` targets 99.9% uptime — achievable for a stateless, read-only service with no database dependency.
- **Correctness:** 100% of generated projects compile, pass their included tests, and start successfully across all supported project type × build system combinations (MVP: 2 types × 3 build systems = 6 combinations; expanding to 15 as project types are added in subsequent phases). This implies a required CI test matrix as a non-negotiable implementation constraint. Template changes on a PR trigger a targeted integration workflow that validates only the affected combinations — generate, build, and start — as a merge gate.
- **Channel consistency:** The web UI, REST API, CLI, and `mvn archetype:generate` must invoke the same generation engine — no divergence between channels is acceptable.
- **Version currency:** The starter is updated within 24 hours of a new Operaton stable release.
- **Self-hostability:** The Docker image runs with zero configuration beyond environment variables — no external service dependencies at startup.

### Measurable Outcomes

| Outcome | Target |
|---------|--------|
| UI-to-ZIP download time | ≤ 30 seconds |
| REST API generation time | ≤ 1 second |
| Generated project compile rate | 100% |
| Generated project CI pass rate | 100% |
| Version update lag after Operaton release | ≤ 24 hours |
| Availability (public instance) | 99.9% |

---

## Product Scope

### MVP — Minimum Viable Product

Core generation engine supporting **2 project types (Process Application, Process Archive)** across Maven and Gradle (Groovy or Kotlin DSL) — 6 combinations total; Gradle DSL choice is a sub-option that appears only when Gradle is selected. Identity-aware scaffolding; each generated project is a **complete, working example** — graphically valid BPMN diagram, implemented delegates (not stubs), elaborated file structure with domain logic and tests in separate packages; deployment-target selector for Process Archive; always-latest Operaton version.

Web UI at `start.operaton.org`: split landing (form + gallery), live file tree preview with clickable file content pane, capability tags, shareable config links, inline "explain this" help. Gallery second section: 4 use case examples (Leave Request, Loan Application, Incident Management, Order Fulfillment) — each self-contained, out-of-the-box runnable, with character-narrated onboarding README.

Generated project extras (all off by default): purposeful README with next-steps (always included), optional Docker Compose, optional GitHub Actions CI/CD skeleton (green on first push), optional Dependency Updates (Dependabot or Renovate — flavour selected as a sub-option).

API & ecosystem: REST API (`POST /api/v1/generate`), MCP module, `curl`/`npx operaton-starter` support, open-source generation templates, archetype-as-source-of-truth (web UI and `mvn archetype:generate` share the same engine).

Infrastructure: deployed at `start.operaton.org`, self-hostable Docker image configurable via environment variables.

*Detailed phasing rationale and post-MVP roadmap: see Project Scoping & Phased Development.*

### Growth Features (Post-MVP)

- Interactive CLI mirror — `npx operaton-starter` with full interactive prompts
- Social proof usage stats (requires opt-in telemetry data)
- Opt-in anonymous telemetry
- "What's New" release banner
- Inline documentation links in generated files
- Tested stack snapshots
- Multi-module project generation
- Backstage Software Template plugin
- Git repository push *(requires OAuth — breaks stateless principle; requires explicit design decision before implementation)*
- Community configuration gallery *(requires persistent storage — breaks stateless principle; requires explicit design decision before implementation)*
- Formula / recipe system
- Upgrade-aware Maven plugin (`mvn operaton-starter:upgrade`)

### Vision (Future)

Operaton Starter becomes the **inevitable front door** of the Operaton developer journey — the single URL referenced in every getting-started guide, conference talk, and migration tutorial. The community archetype registry enables third-party templates (consulting firm blueprints, ISV integrations, platform-specific presets) to appear in the gallery without Operaton core team involvement. The MCP module becomes the standard way AI coding assistants scaffold Operaton projects mid-conversation.

---

## User Journeys

### Journey 1: Marcus — The Practitioner (Happy Path)

**Persona:** Marcus is a senior Java developer at a logistics company. His team has been running Operaton in production for eight months. He's been asked to spin up a new process application for a shipment tracking workflow. He knows exactly what he needs: Spring Boot, Gradle Kotlin DSL, Renovate.

**Opening Scene:** Marcus has a new GitHub repo open in one tab and `start.operaton.org` in another. He's done this before — not with Operaton, but with Spring Initializr and code.quarkus.io. He expects it to work the same way.

**Rising Action:** He lands on the form view. Group ID: `com.acme.logistics`. Artifact: `shipment-tracking`. Project type: Process Application. Build: Gradle Kotlin DSL. He checks Dependency Updates and selects Renovate. He toggles Docker Compose on. The live preview panel updates with every selection — he can see `shipment-tracking.bpmn`, `build.gradle.kts`, `docker-compose.yml`, and `Dockerfile` appearing in the file tree. He clicks Download.

**Climax:** Marcus unzips the archive into his repo, opens it in IntelliJ. `build.gradle.kts` is open. The BPMN file is in `src/main/resources`. There is nothing to configure.

**Resolution:** Marcus runs `./gradlew bootRun`. The Operaton engine starts. He opens Cockpit at the URL shown in the generated README — the README knows his configured server port (default: 8080), so the link is accurate. The skeleton process is deployed. Total time from landing to running engine: under 3 minutes. He copies the shareable link and sends it to his colleague starting the companion process archive.

**Edge Case — Project Fails to Start:** If Marcus's project fails to start (e.g., port conflict, missing datasource), the generated README contains a "Troubleshooting" section with the most common startup failure modes and their resolutions, including a port-specific instruction that references the actual configured port. The README is the first line of support — it is generated specifically for his stack, not generic boilerplate.

**Capabilities revealed:** form view, build system selection, live preview, Extras options (Dependency Updates → Renovate, Docker Compose toggle including Dockerfile), shareable config link, identity-aware scaffolding, skeleton BPMN, zero-boilerplate startup, port-aware troubleshooting README.

---

### Journey 2: Elena — The Camunda Migrator (Explorer)

**Persona:** Elena is a process architect at a mid-sized bank. She has managed five Camunda 7 process applications over four years. With Camunda 7 EOL looming, her team is evaluating Operaton as the migration path. She's been told "it's mostly compatible" but has no practical experience yet. She carries the quiet anxiety of someone who has survived painful migrations before.

**Opening Scene:** Elena finds `start.operaton.org` linked from the Operaton migration guide. She notices "Camunda 7 Migration" as a dedicated gallery card — not hidden in a dropdown, not a footnote. A signal that Operaton takes this seriously.

**Rising Action:** She clicks the migration card. The form pre-fills with migration-specific options. The live preview shows a `MIGRATION.md` checklist: dependency substitutions, renamed API mappings, manual migration TODOs categorised by effort level. The inline help explains what OpenRewrite recipes do and links to the `operaton/migrate-from-camunda-recipe` repository. She downloads the ZIP and runs the recipe on her smallest existing Camunda 7 project — 80% of mechanical changes apply automatically.

**Climax:** She opens `MIGRATION.md`. The manual checklist is structured, specific, and scoped. She can estimate the remaining work in 20 minutes — something that previously would have required a two-day spike.

**Resolution:** The anxiety Elena carried walking in — *"is this migration going to be a nightmare?"* — is replaced by a concrete plan with a realistic scope. She presents the checklist to her team lead as a migration project proposal. The starter didn't just generate code; it gave her the confidence to commit to a migration path. She books the spike.

**Post-download README moment:** The README explains how to apply the OpenRewrite recipe, what to expect, and what the manual checklist covers — written for someone who has never used OpenRewrite, not someone who already knows it.

**Capabilities revealed:** migration gallery card, migration project type, `MIGRATION.md` generation, OpenRewrite recipe integration, inline contextual help, Explorer-oriented README.

---

### Journey 3: Thomas — The BPMN Newcomer (Explorer, Edge Case)

**Persona:** Thomas is a Spring Boot developer who has just joined a team that uses Operaton. He understands Java and Spring Boot deeply but has never worked with BPM tooling. He's been asked to create "a process archive for the approval workflow" and doesn't yet know what that means.

**Opening Scene:** Thomas lands on `start.operaton.org` and gravitates to the gallery. He sees "Process Archive" and "Process Application" as separate cards and isn't sure which one he needs.

**Rising Action:** He hovers over the "?" icon next to "Process Archive." The inline help expands: *"A Process Archive is an engine-agnostic deployable (WAR/JAR) for a Standalone Engine — use this when your organisation runs a shared Operaton server."* He checks with his team lead — yes, they run a shared engine. He selects Process Archive. A "Target Runtime" option appears — he selects Tomcat. The live preview shows `processes.xml` with the correct archive name and engine reference pre-configured.

**Climax:** Thomas downloads the ZIP and deploys `target/approval-workflow.war` to Tomcat on his first attempt. The deployment succeeds.

**Resolution:** Thomas didn't need to understand the full Operaton architecture to succeed. The gallery helped him discover his project type, the inline help taught him the distinction between a Process Archive and a Process Application, and the live preview showed him what `processes.xml` was before he ever had to read the documentation. He didn't just build something — he *learned* what he was building and why. He bookmarks `start.operaton.org` as the entry point for every new Operaton module his team creates.

**Post-download README moment:** The README's "What to do next" section links directly to the deployment descriptor documentation — the one concept Thomas will need to understand next, surfaced at exactly the right moment.

**Capabilities revealed:** gallery-first discovery, inline contextual help, project type disambiguation, deployment-target selector, identity-aware `processes.xml` generation, learning-oriented README with contextual doc links.

---

### Journey 4: Priya — The Platform Engineer (API Consumer)

**Persona:** Priya is a senior platform engineer at a large enterprise. Her team maintains an internal developer portal (Backstage) used by 200+ developers. Operaton is being adopted company-wide and she needs to add a standardised Operaton project template to the portal.

**Opening Scene:** Priya doesn't use browser-based generators — she integrates their APIs into internal tooling. She finds the Operaton Starter REST API documentation.

**Rising Action:** She tests `POST /api/v1/generate` with the documented `curl` command, customising for their standard: group ID `com.enterprise`, Gradle Kotlin DSL, Renovate, Docker Compose. The ZIP returns in under a second. She wraps the API call in a Backstage Software Template with enterprise defaults pre-filled — internal Maven registry, standard group ID prefix, mandatory Renovate config. She also integrates the MCP module so developers can describe their process in natural language and the AI assistant generates the configuration and calls the API automatically.

**Climax:** The Operaton template appears in the internal developer portal alongside Spring Boot and Quarkus templates. Developers generate Operaton projects the same way they generate any other project — through the portal, with enterprise defaults applied, without visiting any external website.

**Resolution:** Priya self-hosts the Docker image behind the firewall. All generation stays internal. The self-hosted instance is configured with `DEFAULT_GROUP_ID=com.enterprise` and their internal Nexus registry URL. Operaton project scaffolding is now a governed, standardised, enterprise-grade offering. Priya schedules quarterly image updates aligned with Operaton release cadence.

**Capabilities revealed:** REST API with curl examples, MCP module, self-hostable Docker image, environment variable configuration, API documentation quality, stateless design enabling enterprise integration.

---

### Journey 5: Klaus — The Self-Hosted Admin (Operations)

**Persona:** Klaus is a platform engineer at a financial institution with strict policies: no external SaaS tools, all developer tooling must run on-premises. His team wants to offer Operaton Starter to 50 internal developers.

**Opening Scene:** Klaus finds the self-hosting documentation. He sees a `docker run` command with environment variable configuration and no external dependencies listed.

**Rising Action:** He pulls the image, sets `DEFAULT_GROUP_ID=com.bank`, `MAVEN_REGISTRY=https://nexus.bank.internal/repository/maven-public`, `OPERATON_VERSION=1.0.0` (pinned for internal certification compliance). He runs `docker compose up`. The service starts with no external calls at startup. He configures the reverse proxy at `start.operaton.internal`.

**Climax:** Klaus sends the internal URL to development teams. The tool is visually identical to the public instance. Group ID is pre-filled with `com.bank`. All Maven dependencies resolve from Nexus. No security policy exceptions required.

**Resolution:** 50 Operaton developers have a project generator that fits their compliance constraints. Klaus has a simple operational model: pull the new image quarterly, verify it starts, update the compose file. No database to maintain, no user data to manage, no external dependencies to monitor.

**Capabilities revealed:** self-hostable Docker image, environment variable configuration (`DEFAULT_GROUP_ID`, `MAVEN_REGISTRY`, `OPERATON_VERSION`), zero external startup dependencies, identical UX to public instance, simple operational model.

---

### Journey Requirements Summary

| Journey | Key Capabilities Required |
|---------|--------------------------|
| Marcus (Practitioner) | Form view, live preview, Extras options (Dependency Updates, Docker Compose), build system choice, shareable links, skeleton BPMN, troubleshooting README |
| Elena (Migrator) | Migration gallery card, `MIGRATION.md`, OpenRewrite integration, inline help, migration README |
| Thomas (Newcomer) | Gallery, inline help, deployment-target selector, learning README with doc links, `processes.xml` generation |
| Priya (API Consumer) | REST API, `operaton-starter-mcp` npm package, curl examples, self-hosted Docker, enterprise env-var config |
| Klaus (Admin) | Self-hosted Docker, env-var defaults, zero external deps, version pinning, simple ops model |

---

## Innovation & Novel Patterns

### Detected Innovation Areas

**Ecosystem First-Mover**
No project initializer for Operaton exists. Operaton Starter is not incrementally improving an existing tool — it is creating the category for Operaton. The innovation is occupancy: claiming the "project generator" position in the Operaton ecosystem before anyone else.

**`operaton-starter-mcp` npm Package — AI-Native Project Generation**
The `operaton-starter-mcp` npm package exposes `POST /api/v1/generate` as a callable MCP tool for AI coding assistants (Claude, GitHub Copilot, Cursor). It is independently published on npm with its own versioning lifecycle — discoverable by anyone searching for "operaton MCP" or "operaton AI", creating a second acquisition channel independent of `start.operaton.org`. No comparable BPM initializer offers AI-native project generation. A developer describing "I want to build a loan approval process" in an AI assistant can receive a generated project scaffold directly in the conversation, without opening a browser. Every `operaton-starter-mcp` user is one README link away from becoming a `start.operaton.org` user.

**Unified Generation Engine Across All Channels**
The web UI, REST API, `npx operaton-starter` CLI, and `mvn archetype:generate` all invoke the same archetype engine. No BPM initializer currently maintains this channel consistency — start.camunda.com has no API, code.quarkus.io has API but no Maven archetype parity. The unified engine is both an architectural decision and a developer trust signal.

**Migration as a Project Type**
Treating Camunda 7 migration as a first-class project type — not a FAQ page or a blog post — and backing it with the `operaton/migrate-from-camunda-recipe` OpenRewrite tooling transforms migration from a manual effort into a structured, tool-assisted process initiated from the same entry point as all other Operaton projects.

### Market Context & Competitive Landscape

No BPM project initializer currently offers: REST API + CLI + Maven archetype parity + `operaton-starter-mcp` npm package + first-class migration. start.spring.io is the reference for UX quality. code.quarkus.io is the reference for extension discovery UX. Neither operates in the BPM space. The BPM-specific competitor (start.camunda.com) has no API, no CLI, no migration mode, and stale defaults.

The repository spans Java (server, templates, archetypes) and JavaScript (Node.js `operaton-starter-mcp` npm package, potentially the web UI). This is intentional — the MCP module's npm distribution requires a Node.js package.

### Validation Approach

- **`operaton-starter-mcp`:** Validate by registering the npm package with Claude Code and generating a project mid-conversation. Success = valid ZIP returned in under 1 second, project compiles.
- **Channel consistency:** Validate by running the CI test matrix (15 combinations) against all three channels simultaneously — web API, `npx operaton-starter`, and `mvn archetype:generate`. Output must be **functionally identical** (same files, same content, same project structure — generation timestamps excepted).
- **Migration project type:** Validate by running the generated migration scaffold against a known Camunda 7 sample project. Success = 80%+ of dependency substitutions applied automatically, `MIGRATION.md` checklist is accurate and complete.

### Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| MCP spec evolves, breaking `operaton-starter-mcp` | Pin to MCP spec version; monitor spec releases as part of Operaton release cycle; semantic versioning on npm package |
| `migrate-from-camunda-recipe` goes unmaintained | Fork under the Operaton org if upstream maintenance lapses; track as an explicit dependency |
| Archetype engine adds latency that breaks ≤1s target | Benchmark in CI; if archetype invocation is slow, pre-compile archetypes at deploy time |
| Community templates diverge from core quality standards | Define a template contribution checklist; CI matrix validates all community templates on PR |

---

## Developer Tool Specific Requirements

### Web Application

- **Architecture:** Single Page Application (SPA)
- **Accessibility:** WCAG 2.1 AA compliance; explicit requirement for **keyboard-complete flow** — the full configuration-and-download journey must be completable without a mouse (tab order, visible focus rings, Enter to submit)
- **Browser support:** Latest 2 versions of Chrome, Firefox, Safari, Edge
- **Live preview:** File tree updates ≤200ms after any configuration change; preview renders **client-side from the metadata payload** — no server round-trip required for preview updates; clicking a file in the tree shows its content inline without triggering generation
- **Responsive:** Desktop-first; mobile: usable, not optimised

### REST API

**Developer-Facing Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/generate` | Generate project archive. `Accept: application/zip` returns ZIP (only supported format at this time). Request body: project configuration JSON. |
| `GET` | `/api/v1/metadata` | Retrieve all configuration options (project types, build systems, etc.) and **project template manifests** — the set of files generated per project type and their conditions (e.g., `docker-compose.yml` included if `dockerCompose: true`), enabling client-side preview rendering without server round-trips. |
| `GET` | `/api/v1/docs` | OpenAPI 3.x specification (generated from the spec-first source) and interactive Scalar API documentation UI rendered via static HTML loading Scalar from CDN |

**Operational Endpoints (infrastructure, not part of developer API contract):**

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/actuator/health` | Spring Boot health check for load balancer and ops monitoring |

**API Design Principles:**
- Rate limiting: 10 requests/minute per IP; HTTP 429 + `Retry-After` header on breach
- **Spec-first:** The API contract is defined in `openapi.yaml` first; server stubs and client code (including the `operaton-starter-mcp` client) are generated via the OpenAPI Generator tool — channel consistency enforced by construction
- Base path: `/api/v1/` — versioned from day one

### CLI — `npx operaton-starter`

- **Dual mode:**
  - *Scriptable:* when stdout is a pipe, outputs raw ZIP bytes (suitable for `> project.zip` or piped extraction); all options passed as flags
  - *Interactive:* when stdout is a terminal and no flags provided, guided prompt sequence
- `--output <dir>` flag: extract ZIP into specified directory
- `--extract` flag: automatically extract ZIP in place
- **Thin wrapper:** CLI invokes the REST API; no independent generation logic

### `operaton-starter-mcp` npm Package

- Thin Node.js wrapper around `/api/v1/generate`; **client generated from the OpenAPI spec**
- Independently published on npm (`operaton-starter-mcp`) with own versioning lifecycle
- Defaults to `https://start.operaton.org` as base URL; overridable via env var for self-hosted instances
- Exposes `generate_project` as an MCP tool

### Monorepo Structure

```
operaton-starter/
├── starter-server/        # Spring Boot application (Java)
├── starter-templates/     # Generation templates (Java)
├── starter-archetypes/    # Maven archetypes (Java)
├── starter-mcp/           # operaton-starter-mcp npm package (TypeScript)
└── starter-web/           # Web UI (TypeScript)
```

### Metadata as Source of Truth

- `/api/v1/metadata` is the single source of all configuration options and template manifests
- Web UI, CLI, and `operaton-starter-mcp` all fetch from `/api/v1/metadata` to populate their option sets and render client-side previews
- No hardcoded option lists in any channel — all driven from the metadata endpoint
- OpenAPI spec is the contract source of truth for the API layer; generated code must not diverge from it

---

## Project Scoping & Phased Development

### MVP Strategy & Philosophy

**MVP Approach:** Platform/Ecosystem MVP — claim the full Operaton project-generation category on day one with all channels active. Partial launch (e.g., web-only) would undermine the "ecosystem front door" positioning.

**Resource Profile:** Solo developer. CLI and `operaton-starter-mcp` are generated thin wrappers (~1 day each once the API is solid); real build complexity concentrates in the REST API + template engine + web UI.

### Phase 1 — MVP

**Project types supported:**
- Process Application (Spring Boot embedded engine)
- Process Archive (engine-agnostic WAR/JAR for Standalone Engine)

**Build systems:** Maven; Gradle with Groovy DSL; Gradle with Kotlin DSL — selected as a two-level choice (build system, then DSL if Gradle); 6 combinations total

**All channels ship in MVP:** Web UI, REST API (`POST /api/v1/generate`), CLI (`npx operaton-starter`), `operaton-starter-mcp` npm package

### Phase 2 — Growth

- **Camunda 7 Migration** project type (OpenRewrite wrapper — `operaton/migrate-from-camunda-recipe`); simpler than its strategic value suggests
- Social proof: opt-in anonymous telemetry, usage stats display
- Interactive CLI full prompt mode
- "What's New" release banner

### Phase 3 — Expansion

- **Engine Plugin** project type
- Backstage Software Template plugin
- Multi-module project generation
- Tested stack snapshots

### Phase 4 — Advanced

- **Connector** project type
- Community configuration gallery *(requires stateless-principle design review before implementation)*
- Git repository push *(requires OAuth — breaks stateless principle; requires explicit design review before implementation)*
- Formula/recipe system
- Upgrade-aware Maven plugin (`mvn operaton-starter:upgrade`)

### Phasing Rationale

Project types are phased by adoption value, not technical complexity. Process Application and Process Archive represent the core "start a new Operaton project" use case — the highest-frequency need. Camunda 7 Migration targets an active migration audience with high strategic value. Engine Plugin and Connector serve a narrower, more advanced audience and are deferred to maintain MVP focus.

### Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| **Technical: spec drift** | Spec-first discipline: API spec must be frozen before client generation begins; any post-freeze change requires regenerating all clients |
| **Technical: template combinatorics** | Start with 2 project types × 3 build systems = 6 combinations; validate generation pipeline end-to-end before adding types |
| **Resource: solo developer** | CLI and MCP are thin generated wrappers; complexity budget concentrated on API + template engine + web UI |
| **Market: low initial adoption** | All channels live on day one maximises discovery surface; `start.operaton.org` URL provides first-party authority |

---

## Functional Requirements

### Project Generation Engine

- **FR1:** The system can generate a project archive for any supported project type × build system combination
- **FR2:** The system generates projects that compile and pass their included tests without manual modification; generated projects are complete, working examples — not minimal scaffolds — with meaningful process logic, valid BPMN diagrams, and implemented delegates that a developer can run, study, and extend
- **FR3:** The system always generates projects targeting the current stable Operaton release (no user-selectable version)
- **FR4:** The system propagates developer identity (Group ID, Artifact ID, project name) consistently across all generated files — Java package names, BPMN process IDs, Spring application name
- **FR5:** The system generates a BPMN process file for applicable project types; the BPMN file contains a complete, graphically valid diagram — all flow elements include `BPMNShape` and `BPMNEdge` layout data so the process renders correctly in any BPMN-aware tool without manual repositioning
- **FR6:** The system generates a `processes.xml` deployment descriptor for Process Archive projects, pre-configured with the selected deployment target
- **FR7:** The system generates target-platform-appropriate artifact configuration (WAR/JAR) for Process Archive projects, matching the platform selected via FR12
- **FR8:** The generation engine is a single shared implementation invoked by all channels — web UI, REST API, CLI, and MCP module; no per-channel generation logic
- **FR42:** The CLI and `operaton-starter-mcp` client code are generated from the OpenAPI specification; no hand-written client code exists independently of the API contract

### Project Configuration

- **FR9:** A developer selects a project type (MVP: Process Application, Process Archive) from the gallery/landing page before reaching the configuration details page; project type selection is a prerequisite to configuration, not a field on the configuration form
- **FR10:** A developer selects a build system in two steps: first choosing between Maven and Gradle; if Gradle is chosen, a DSL sub-option (Groovy or Kotlin) becomes visible and must be selected before generation; the DSL sub-option is hidden when Maven is selected
- **FR11:** A developer can specify Group ID, Artifact ID, and project name as project identity
- **FR12:** A developer can select a target platform for Process Archive projects (MVP platforms: Tomcat, Wildfly; list is extensible); the selected platform determines artifact type (WAR/JAR) and deployment descriptor pre-configuration
- **FR13:** Dependency update configuration is an opt-in Extras option (unchecked by default); when a developer checks it, a sub-option for flavour (Dependabot or Renovate) becomes visible and must be selected before generation; the sub-option is hidden when the Dependency Updates option is unchecked
- **FR14:** A developer can opt in to Docker Compose file generation; this option is only presented for project types that support containerised embedded deployment (Process Application); it is not shown for Process Archive projects
- **FR15:** A developer can opt in to GitHub Actions CI/CD skeleton generation
- **FR56:** All Extras options (Dependency Updates, Docker Compose, GitHub Actions) are unchecked/off by default; a developer must explicitly enable each one
- **FR16:** A developer can share a project configuration as a URL that restores and pre-fills the configuration form when opened

### Web UI

- **FR17:** A developer can configure a project and download a ZIP archive through a browser
- **FR60:** The web UI serves a `favicon.ico` derived from the Operaton logo so that browser tabs and bookmarks display the Operaton brand mark; no 404 error is emitted for `/favicon.ico` requests
- **FR18:** A developer can browse available project types as a visual gallery with capability descriptions
- **FR19:** A developer can see a live file tree preview of the project to be generated, updated as configuration options change; the preview is interactive — selecting a file shows its content
- **FR20:** A developer can access inline contextual help for any configuration option without leaving the page
- ~~FR21 — IDE deep-links (IntelliJ IDEA, VS Code): removed. Browser-to-IDE protocol handlers (`idea://`, `vscode://`) require the IDE to be installed and registered as a URL handler, which cannot be guaranteed; the feature is not reliably deliverable across environments and has been explicitly descoped. Download + manual import remains the supported path.~~
- **FR22:** A developer can complete the full configuration and download flow without using a mouse
- **FR23:** The web UI populates all configuration options and gallery content from the REST API metadata endpoint
- **FR40:** A developer can access the tool through two distinct entry points: a direct configuration form (for developers who know what they want) and a project gallery (for discovery-oriented developers), with both leading to the same generation flow; the gallery landing page (`/`) presents project types first, followed by pre-configured use-case examples — this order ensures developers understand the available project types before seeing curated examples built on top of them
- **FR67:** The gallery displays a curated set of use case examples as a second section below the project types; each example card shows a title, one-sentence description, and capability tags; clicking a card pre-fills the configuration form and navigates to the details page; the four MVP use case examples are defined in the section below
- **FR41:** A developer can access an explanation distinguishing between available project types to inform their selection
- **FR45:** The configuration details page requires a project type to be present in the route/navigation state; if a developer navigates directly to the details page without a project type (e.g., via bookmarked URL with no project type param), they are redirected to the gallery to make a selection first; once on the details page, the project type is displayed as read-only context (e.g., a header or badge) and is not an editable field — there is no project type selector on the details page
- **FR46:** Configuration options on the details page are conditionally rendered based on the project type carried in from the gallery; options that do not apply to the selected project type are hidden entirely (not shown as disabled); to use a different project type, the developer returns to the gallery and selects again — the details page never exposes a project type control
- **FR43:** The web UI renders the project file tree preview from template manifests in the metadata response, without a per-change server round-trip
- **FR57:** When a developer clicks a file in the File Structure Preview, the file's representative content is shown in a content pane adjacent to the file tree; the content pane updates when a different file is selected and whenever any configuration value changes; no download or generation step is required to see the content; the content pane renders the actual content derived from the current form state — template placeholders in `TemplateManifestEntry.previewContent` are substituted client-side with the current configuration values (Group ID, Artifact ID, project name, build system, selected extras, etc.) so the developer sees exactly what the generated file will contain for their configuration; this substitution is performed in the browser without a server round-trip, preserving the no-server-round-trip invariant from FR43; `TemplateManifestEntry.previewContent` contains named placeholders (e.g., `{{groupId}}`, `{{artifactId}}`) that the client replaces with the live form values on every change
- **FR76:** The content pane must reactively re-render when any configuration value changes while a file is open — including changes to Group ID, Artifact ID, project name, Java version, build system, or any extras — so the displayed content always reflects the current form state without requiring the developer to re-select the file
- **FR77:** If a file is currently open in the content pane and a configuration change causes that file to no longer appear in the file tree (e.g., switching from Maven to Gradle removes `pom.xml`), the content pane must immediately clear and show an empty state; no stale content from a no-longer-applicable file must remain visible

- **FR78:** Each use case example entry in the metadata response carries its own `templateManifest` — the list of files specific to that use case, including the use-case-specific BPMN — so that the File Structure Preview and content pane show the actual use case workflow instead of the generic skeleton process

### Use Case Examples

Use case examples are pre-configured Process Application projects available through all generation channels (web UI gallery, REST API, CLI, MCP). Each example is identified by a stable `useCaseId` (e.g., `leave-request`, `loan-application`) that is discoverable via `GET /api/v1/metadata` and passable to `POST /api/v1/generate`; the server resolves the `useCaseId` to a fixed parameter bundle and generates the project using the same engine as any other request — no separate generation path exists. Each is **self-contained and out-of-the-box runnable**: `docker compose up -d` followed by `./mvnw spring-boot:run` produces a working, explorable application with no manual configuration. All examples use PostgreSQL started via Docker Compose as the default datasource; examples that also stub external APIs include WireMock as a second service in the same compose stack — a single `docker compose up -d` starts everything. Operaton's built-in Tasklist is the human task UI for all examples; no custom frontend is generated.

**Design principles shared across all examples:**
- User roles and groups are seeded via `data.sql` at startup — not application startup code — making the user set declarative and visible
- User names are human (e.g., `alice`, `bob`) rather than generic (`user1`, `admin`); passwords match the username for discoverability
- An Operaton admin user is always created at startup if it does not already exist — the startup sequence checks for the admin account and creates it on first boot so the developer never encounters an empty Cockpit login with no credentials
- The generated README uses character-narrated onboarding: it names the pre-seeded users, walks the developer through the Tasklist as those characters so the first-run experience feels directed rather than exploratory, and includes a "Bootstrap Data" section explaining how to apply or re-apply `data.sql` and what each seed entry provides
- The generated README includes an embedded or linked image of the BPMN model for the use case, so the developer can orient themselves visually before launching the app
- On Mac and Linux the build wrapper scripts (`mvnw`, `gradlew`) must be made executable before first use; the README includes a one-line `chmod +x mvnw` (or `chmod +x gradlew`) instruction immediately before the first run command
- Each example's integration test suite includes an assertion that the process definition is deployed and the engine is reachable before exercising any business-logic assertions — this catches silent BPMN deployment failures caused by incorrect classpath paths
- Examples that use timer events include a test-profile mechanism ensuring timers fire within seconds during `mvn test`, without relying on wall-clock sleep; the test build must activate this profile (e.g. via Maven Surefire configuration or `@ActiveProfiles`) so the override is guaranteed to apply
- All examples use **PostgreSQL as the default datasource**, started via Docker Compose; switching to the embedded H2 database requires only a Spring profile change (`--spring.profiles.active=h2`) and zero code changes — the `application-h2.properties` profile file is included in every generated example and the README documents the switch with a single command

**MVP use case examples (4):**

| ID | Name | External (Docker) | Key BPMN concept | User roles |
|----|------|-------------------|-----------------|------------|
| UC-01 | Leave Request | Postgres | User tasks, candidate groups | `alice` (employee), `bob` (manager), `carol` (HR) |
| UC-02 | Loan Application | Postgres + WireMock | DMN decision + service tasks | `jack` (underwriter), `kate` (applicant) |
| UC-03 | Incident Management | Postgres + WireMock | Timer boundary event + escalation | `henry` (first-line), `iris` (second-line) |
| UC-04 | Order Fulfillment | Postgres + WireMock | Service tasks + conditional routing | `dave` (warehouse) |

**UC-01 — Leave Request**
- BPMN: `Start(employee submits) → UserTask(manager reviews) → Gateway → [approved] UserTask(HR records) → End / [rejected] UserTask(employee notified) → End`
- Default datasource: PostgreSQL via Docker Compose; H2 fallback available via `--spring.profiles.active=h2`
- Teaches: embedded Operaton, Tasklist, candidate groups, BPMN happy/rejection paths
- Post-start: `bob/bob` logs into Tasklist at `http://localhost:8080/operaton/app/tasklist` and finds one waiting task

**UC-02 — Loan Application**
- BPMN: `Start → ServiceTask(credit score check [REST]) → BusinessRuleTask(DMN: risk assessment) → Gateway → [low risk] auto-approve / [medium risk] UserTask(underwriter review) / [high risk] ServiceTask(auto-reject)`
- DMN file: `risk-assessment.dmn` (inputs: `creditScore`, `loanAmount` → output: `riskLevel`: low/medium/high); hit policy `FIRST`
- Default datasource: PostgreSQL via Docker Compose (alongside WireMock); H2 fallback via `--spring.profiles.active=h2`
- External: WireMock stubs credit-score API; stubs committed in `src/main/resources/wiremock/mappings/`
- Teaches: DMN business rules alongside BPMN, service task HTTP integration, decision-driven branching
- DMN engine support is required; the generated project must explicitly declare this dependency (it is not guaranteed by transitive resolution from the base Operaton starter)

**UC-03 — Incident Management**
- BPMN: `Start(incident reported) → UserTask(first-line triage) [BoundaryTimerEvent PT1H → escalate] → Gateway → [resolved] ServiceTask(close [REST]) → End / [timer] UserTask(second-line engineer) → ServiceTask(post-mortem notify [REST]) → End`
- Default datasource: PostgreSQL via Docker Compose (alongside WireMock); H2 fallback via `--spring.profiles.active=h2`
- External: WireMock stubs close-ticket and notify APIs
- Teaches: boundary timer events, SLA escalation, test-profile-controlled time advancement without wall-clock sleep
- Timer constraint: the generated project must include a test-profile mechanism that shortens the boundary timer for testing; the test build must guarantee this profile is active during `mvn test`; `ClockUtil` is used to advance the engine's internal clock; `ClockUtil.reset()` must be called after each timer-dependent test to prevent cross-test pollution

**UC-04 — Order Fulfillment**
- BPMN: `Start(order placed) → ServiceTask(validate inventory [REST]) → Gateway → [in stock] ServiceTask(charge payment [REST]) → UserTask(pack & ship) → ServiceTask(notify customer [REST]) → End / [out of stock] ServiceTask(notify backorder [REST]) → End`
- Default datasource: PostgreSQL via Docker Compose (alongside WireMock); H2 fallback via `--spring.profiles.active=h2`
- External: WireMock stubs inventory, payment, and notification APIs
- Teaches: multi-step service task orchestration, conditional routing on external API results, mixed service/human task process
- WireMock startup: test must wait for `/__admin/mappings` health check before first service task invocation

**Post-MVP use case examples** (deferred because the external dependency requires more onboarding friction than the MVP stack and is lower-frequency as a teaching goal for the initial Explorer audience; these will be planned and scoped individually):
- `document-approval` — multi-level review with MinIO file archive (3 user roles, MinIO Docker service; deferred: file storage is infrastructure, not process logic — Explorer audience learns Operaton, not MinIO)

- **FR68:** Each MVP use case example generates a project that satisfies the self-containment invariant: `docker compose up -d` followed by `./mvnw spring-boot:run` starts successfully with no manual configuration; all included JUnit tests pass; each example's integration test suite must include an assertion verifying that the process definition is deployed and the engine is reachable before any business-logic assertions execute
- **FR69:** Each MVP use case example seeds its user roles and groups via `data.sql` at startup; username and password are identical for each user (e.g., `alice/alice`); roles map to BPMN `candidateGroups` expressions in the process definition; an Operaton admin user is created at startup if it does not already exist — this is handled in the startup sequence so the developer always has admin access to Cockpit without manual setup
- **FR70:** Every MVP use case example includes a `docker-compose.yml` that starts a PostgreSQL service as the default datasource; examples that also stub external APIs include WireMock as a second service in the same compose stack; all services include health checks and the Spring Boot app depends on them with `condition: service_healthy`; the Spring Boot app runs on the host, not in Docker
- **FR71:** Each MVP use case example includes a character-narrated "Getting Started in 5 Minutes" README section that: names the pre-seeded users and walks the developer through the Tasklist as those characters; includes a "Bootstrap Data" section describing how to apply or re-apply `data.sql` and what each seed entry (users, groups, admin account) provides; includes an image of the BPMN process model so the developer can orient themselves visually before launching the app; and includes a `chmod +x mvnw` (or `chmod +x gradlew`) instruction on Mac/Linux immediately before the first run command
- **FR74:** Every MVP use case example includes an `application-h2.properties` Spring profile file that switches the datasource to embedded H2 with no other code changes; the README documents the switch as a single command (`./mvnw spring-boot:run --spring.profiles.active=h2`) and notes it as a fallback for environments where Docker is unavailable; the H2 profile is also the active profile used during `mvn test` to avoid requiring Docker in CI
- **FR72:** WireMock stub mapping files for examples that use WireMock are committed in `src/main/resources/wiremock/mappings/` and mounted into the WireMock container via a bind-mount in `docker-compose.yml`; no stubs are configured in Java code; the WireMock container image version is pinned to a specific minor version in the template — upgrading the pinned version is an explicit decision, not an automatic pull
- **FR73:** Use case examples are discoverable and generatable via all channels, not just the web UI gallery; `GET /api/v1/metadata` returns the list of available use case examples with their `useCaseId`, display name, description, capability tags, and pre-filled parameter bundle; `POST /api/v1/generate` accepts an optional `useCaseId` parameter which resolves to a fixed parameter bundle on the server and generates the project using the standard engine — no separate generation path exists for use case examples

### REST API

- **FR24:** An API consumer can generate and download a project archive via `POST /api/v1/generate` with `Accept: application/zip`
- **FR25:** An API consumer can retrieve all supported configuration options and project template manifests via `GET /api/v1/metadata`
- **FR26:** An API consumer can access the complete OpenAPI specification and an interactive Scalar API documentation UI at `/api/v1/docs`; the Scalar UI is rendered via a static HTML page loading Scalar from CDN — no Spring Boot version coupling
- **FR27:** The system enforces a rate limit per IP address and returns a structured error response when exceeded

### CLI

- **FR28:** A developer can generate and download a project archive using `npx operaton-starter` with all options as command-line flags
- **FR29:** The CLI outputs the project archive as raw bytes to stdout when stdout is a pipe, enabling shell scripting
- **FR30:** A developer can instruct the CLI to extract the generated archive into a specified directory

### MCP Integration

- **FR31:** An AI assistant can generate an Operaton project archive by invoking the `generate_project` MCP tool from the `operaton-starter-mcp` npm package
- **FR32:** The `operaton-starter-mcp` package can be configured with a custom base URL to point at a self-hosted instance

### Generated Project Quality

- **FR33:** Every generated project includes a README with project-specific next-step instructions tailored to the selected project type and build system; all URLs and commands in the README (Cockpit URL, troubleshooting port instructions, docker-compose launch steps) reflect the actual project configuration (e.g., server port, Docker Compose enabled/disabled); the README includes a `chmod +x mvnw` (or `chmod +x gradlew`) instruction for Mac/Linux users immediately before the first run command
- **FR45:** Every generated project includes the appropriate build tool wrapper — Maven wrapper (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`) for Maven projects, Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.{jar,properties}`) for Gradle projects — enabling the project to be built without a globally installed Maven or Gradle installation
- **FR34:** When a developer opts in to Dependency Updates (FR13), the generated project includes a configured dependency update file (Dependabot or Renovate, per their sub-option selection) ready to use without modification; projects generated without this option enabled do not include a dependency update file
- **FR35:** Generated Process Application projects include a GitHub Actions CI/CD workflow that passes on first push
- **FR36:** Generated projects with Docker Compose enabled include a `docker-compose.yml` that starts the application and a multi-stage `Dockerfile` (Maven build stage + runtime image) for containerised builds
- **FR44:** Generated projects include complete, runnable delegate implementations — not stub placeholders — wired to the BPMN service tasks; each delegate performs a meaningful operation representative of the project type (e.g. a Process Application delegate logs execution context and sets an output variable; a DMN project includes an evaluator delegate that invokes a decision table); a JUnit test deploys and executes the full process end-to-end without modification
- **FR75:** Generated Spring Boot Process Application projects include the Operaton `banner.txt` at `src/main/resources/banner.txt`; the file is sourced from the upstream `operaton/operaton` Spring Boot Starter and displays the Operaton ASCII logo alongside the resolved Spring Boot version, Operaton version, and Operaton Spring Boot Starter version on every application startup; no additional configuration is required — Spring Boot's banner mechanism picks it up automatically from the classpath
- **FR58:** Generated projects have an elaborated, well-separated file structure appropriate to the project type: domain logic, process resources, configuration, and tests are placed in distinct packages and source directories; no application logic lives in a default/root package; the structure is a reference example a developer can extend directly, not a flat minimal scaffold
- **FR59:** Every generated project compiles, all included tests pass, and the application starts without errors on first run — the CI test matrix verifies all supported project-type × build-system combinations on every change to the generation templates

### Self-Hosting & Operations

- **FR37:** An operator can deploy Operaton Starter as a self-hosted instance using a single Docker image that bundles the web UI (served as static assets), the REST API, and the generation engine — starting the image produces a fully functional project generator accessible on a single port with no external service dependencies at startup
- **FR47:** The repository contains a `Dockerfile` for building the Operaton Starter application image
- **FR48:** The self-hosted Docker image documents how to connect the `operaton-starter-mcp` npm package to the running instance via the `BASE_URL` environment variable, so AI assistants can use a self-hosted deployment as their generation backend; whether the MCP server process is bundled inside the image alongside the JVM application requires an explicit design decision *(bundling adds a Node.js runtime to the image — trade-off: operational simplicity vs. image size and multi-process complexity)*; the build sequence is: (1) run the Maven build to produce the application JAR (mandatory prerequisite), then (2) build the Docker image from the pre-built JAR — the Docker build itself requires no Maven or internet access and is fully self-contained once the JAR is present
- **FR38:** An operator can configure self-hosted instance defaults (default Group ID, Maven registry URL, Operaton version) via environment variables
- **FR39:** The running instance exposes a health check endpoint for operational monitoring

### Release & Distribution

- **FR49:** Releases are created via a GitHub Actions workflow using **JReleaser**, following the release workflow pattern established in the `operaton/operaton` repository; JReleaser creates the GitHub Release, generates the changelog from conventional commits, and coordinates publishing to all distribution targets in a single automated run
- **FR50:** The Docker image is published to Docker Hub as `operaton/operaton-starter` on every release; image tags follow semantic versioning (`x.y.z`) with a `latest` tag updated on each stable release
- **FR51:** Maven artifacts (generation engine, archetypes, server) are published to Maven Central on every release via the standard Sonatype OSSRH publication flow coordinated by JReleaser
- **FR52:** The `operaton-starter-mcp` npm package is published to the public npm registry (`npmjs.com`) on every release, version-aligned with the overall project release tag
- **FR53:** The repository documentation specifies all credentials that must be configured as GitHub Actions secrets for the release workflow to succeed, including: Docker Hub credentials (`DOCKER_USERNAME`, `DOCKER_PASSWORD`), Maven Central / Sonatype credentials (`MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_TOKEN`), npm publish token (`NPM_TOKEN`), and the GitHub token required by JReleaser for GitHub Release creation; no release should be possible without this credential inventory being current and complete

---

## Non-Functional Requirements

### Performance

- **NFR1:** `POST /api/v1/generate` responds with a complete ZIP within 1 second for up to 10 concurrent requests under normal load
- **NFR2:** Web UI live preview updates within 200ms of any configuration change
- **NFR3:** End-to-end time from UI landing to ZIP download completes within 30 seconds
- **NFR4:** The starter's own repository is configured with Dependabot or Renovate to automatically detect and propose Operaton version bumps

### Availability & Reliability

- **NFR5:** Public instance at `start.operaton.org` achieves 99.9% uptime measured over a rolling 30-day window (consistent with a stateless, database-free service)
- **NFR6:** Self-hosted Docker image starts successfully with no external network calls; the failure of any external service does not prevent project generation; the generation engine has no runtime database dependency — all request state is ephemeral and cleared after each response

### Security

- **NFR7:** All traffic to `start.operaton.org` is served over HTTPS; HTTP requests redirect to HTTPS
- **NFR8:** No user-identifying data is persisted; only transient IP-based data is held for rate limiting enforcement and discarded after the rate-limit window
- **NFR9:** Rate limit enforcement returns HTTP 429 with a `Retry-After` header specifying the retry interval

### Scalability

- **NFR10:** The service is horizontally scalable by adding instances; all instances are interchangeable with no sticky sessions required; no session state or instance-local data is needed to handle any request

### Accessibility

- **NFR11:** The web UI conforms to WCAG 2.1 Level AA; validated using automated accessibility tooling (e.g., axe-core) in CI and manual keyboard navigation testing before each release
- **NFR12:** All web UI functionality is operable via keyboard navigation with visible focus indicators throughout

### Compatibility

- **NFR13:** Generated Process Application projects target Java 21+ and use the Spring Boot version specified in the current Operaton BOM
- **NFR14:** Generated projects using Gradle target Gradle 8+; the bundled Gradle wrapper targets the current pinned Gradle version (currently 8.14)
- **NFR15:** The `operaton-starter-mcp` npm package supports all Node.js Active LTS versions
- **NFR16:** Browser support for the web UI covers the latest 2 major versions of Chrome, Firefox, Safari, and Edge
- **NFR20:** The web UI visual design is consistent with the `operaton.org` and `docs.operaton.org` design system — colors, typography, and component patterns signal the same product family

### Correctness

- **NFR17:** All supported project type × build system combinations (MVP: 6) are validated in CI on every template change; each combination is compiled and its tests executed in a CI matrix job; zero test failures are acceptable; any failure blocks merge
- **NFR21:** On any pull request that modifies generation templates, a dedicated CI workflow — separate from the unit test suite — identifies the project type × build system combinations affected by the changed template files, generates a project for each affected combination, builds the generated project, and starts the application to verify it comes up successfully; all steps must pass for the PR to be mergeable; combinations not touched by the PR's template changes are excluded from that run to keep feedback fast

### Maintainability & Operability

- **NFR18:** The service emits structured JSON logs compatible with standard log aggregation tools
- **NFR19:** The Docker image is configurable entirely via environment variables; no file-based configuration is required at runtime
- **NFR22:** Each submodule in the monorepo (`starter-server`, `starter-templates`, `starter-archetypes`, `starter-mcp`, `starter-web`) has its own `README.md` covering: the submodule's role within the overall system, prerequisites, how to build it in isolation, how to run or use it locally, and at least one concrete usage example; a contributor must be able to build and exercise any submodule using only its README without consulting other sources

