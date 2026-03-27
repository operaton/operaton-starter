---
stepsCompleted: ['step-01-init', 'step-02-discovery', 'step-02b-vision', 'step-02c-executive-summary', 'step-03-success', 'step-04-journeys', 'step-05-domain', 'step-06-innovation', 'step-07-project-type', 'step-08-scoping', 'step-09-functional', 'step-10-nonfunctional', 'step-11-polish', 'step-12-complete']
workflowStatus: complete
completedAt: '2026-03-27'
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

Generated projects support Maven or Gradle (Groovy/Kotlin DSL), always target the current stable Operaton release (no version picker), include a pre-configured Dependabot or Renovate configuration, an optionally generated Docker Compose file, a GitHub Actions CI/CD skeleton that passes green on first push, and a personalized README with next-step instructions. Projects are identity-aware — Group ID, Artifact ID, and project name propagate into BPMN process IDs, Java packages, and Spring `application.name`. Generation templates are open-source and community-forkable. No authentication, no user profiles — stateless by design.

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
- **First-run success:** Every generated project compiles, tests pass, and the application starts with `mvn spring-boot:run` (or Gradle equivalent) without any manual modification. This is a hard quality guarantee, not a target.
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
- **Correctness:** 100% of generated projects compile and pass their included tests across all supported project type × build system combinations (MVP: 2 types × 3 build systems = 6 combinations; expanding to 15 as project types are added in subsequent phases). This implies a required CI test matrix as a non-negotiable implementation constraint.
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

Core generation engine supporting **2 project types (Process Application, Process Archive)** across Maven, Gradle Groovy DSL, and Gradle Kotlin DSL (6 combinations). Identity-aware scaffolding, skeleton BPMN with wired JavaDelegate stub, deployment-target selector for Process Archive, zero boilerplate config classes, always-latest Operaton version.

Web UI at `start.operaton.org`: split landing (form + gallery), live project preview panel, capability tags, shareable config links, IDE deep-link (IntelliJ + VS Code), inline "explain this" help.

Generated project extras: purposeful README with next-steps, optional Docker Compose, GitHub Actions CI/CD skeleton (green on first push), choice of Dependabot or Renovate config.

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

**Persona:** Marcus is a senior Java developer at a logistics company. His team has been running Operaton in production for eight months. He's been asked to spin up a new process application for a shipment tracking workflow. He knows exactly what he needs: Spring Boot, REST starter, Gradle Kotlin DSL, Renovate.

**Opening Scene:** Marcus has a new GitHub repo open in one tab and `start.operaton.org` in another. He's done this before — not with Operaton, but with Spring Initializr and code.quarkus.io. He expects it to work the same way.

**Rising Action:** He lands on the form view. Group ID: `com.acme.logistics`. Artifact: `shipment-tracking`. Project type: Process Application. Build: Gradle Kotlin DSL. He adds the REST starter, enables Renovate, toggles Docker Compose on. The live preview panel updates with every selection — he can see `shipment-tracking.bpmn`, `build.gradle.kts`, `docker-compose.yml` appearing in the file tree. He clicks the IntelliJ deep-link button.

**Climax:** IntelliJ opens with the project already imported, indexed, and ready. `build.gradle.kts` is open. The BPMN file is in `src/main/resources`. There is nothing to configure.

**Resolution:** Marcus runs `./gradlew bootRun`. The Operaton engine starts. He opens Cockpit at `localhost:8080`. The skeleton process is deployed. Total time from landing to running engine: under 3 minutes. He copies the shareable link and sends it to his colleague starting the companion process archive.

**Edge Case — Project Fails to Start:** If Marcus's project fails to start (e.g., port conflict, missing datasource), the generated README contains a "Troubleshooting" section with the three most common startup failure modes and their resolutions. The README is the first line of support — it is generated specifically for his stack, not generic boilerplate.

**Capabilities revealed:** form view, build system selection, live preview, IDE deep-link, Docker Compose toggle, Renovate option, shareable config link, identity-aware scaffolding, skeleton BPMN, zero-boilerplate startup, troubleshooting README.

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
| Marcus (Practitioner) | Form view, live preview, IDE deep-link, build system choice, shareable links, skeleton BPMN, troubleshooting README |
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
- **Live preview:** Updates ≤200ms after any input change; preview renders **client-side from the metadata payload** — no server round-trip required for preview updates
- **Responsive:** Desktop-first; mobile: usable, not optimised

### REST API

**Developer-Facing Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/generate` | Generate project archive. `Accept: application/zip` returns ZIP (only supported format at this time). Request body: project configuration JSON. |
| `GET` | `/api/v1/metadata` | Retrieve all configuration options (project types, build systems, etc.) and **project template manifests** — the set of files generated per project type and their conditions (e.g., `docker-compose.yml` included if `dockerCompose: true`), enabling client-side preview rendering without server round-trips. |
| `GET` | `/api/v1/docs` | OpenAPI 3.x specification (generated from the spec-first source) |

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

**Build systems:** Maven, Gradle Groovy DSL, Gradle Kotlin DSL — 6 combinations total

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
- **FR2:** The system generates projects that compile and pass their included tests without manual modification
- **FR3:** The system always generates projects targeting the current stable Operaton release (no user-selectable version)
- **FR4:** The system propagates developer identity (Group ID, Artifact ID, project name) consistently across all generated files — Java package names, BPMN process IDs, Spring application name
- **FR5:** The system generates a skeleton BPMN process file for applicable project types
- **FR6:** The system generates a `processes.xml` deployment descriptor for Process Archive projects, pre-configured with the selected deployment target
- **FR7:** The system generates deployment-target-appropriate artifact configuration (WAR/JAR) for Process Archive projects
- **FR8:** The generation engine is a single shared implementation invoked by all channels — web UI, REST API, CLI, and MCP module; no per-channel generation logic
- **FR42:** The CLI and `operaton-starter-mcp` client code are generated from the OpenAPI specification; no hand-written client code exists independently of the API contract

### Project Configuration

- **FR9:** A developer can select a project type (MVP: Process Application, Process Archive)
- **FR10:** A developer can select a build system (Maven, Gradle Groovy DSL, Gradle Kotlin DSL)
- **FR11:** A developer can specify Group ID, Artifact ID, and project name as project identity
- **FR12:** A developer can select a deployment target for Process Archive projects
- **FR13:** A developer can choose between Dependabot and Renovate for dependency update configuration
- **FR14:** A developer can opt in to Docker Compose file generation
- **FR15:** A developer can opt in to GitHub Actions CI/CD skeleton generation
- **FR16:** A developer can share a project configuration as a URL that restores and pre-fills the configuration form when opened

### Web UI

- **FR17:** A developer can configure a project and download a ZIP archive through a browser
- **FR18:** A developer can browse available project types as a visual gallery with capability descriptions
- **FR19:** A developer can see a live file tree preview of the project to be generated, updated as configuration options change
- **FR20:** A developer can access inline contextual help for any configuration option without leaving the page
- **FR21:** A developer can open a generated project directly in IntelliJ IDEA or VS Code from the web UI
- **FR22:** A developer can complete the full configuration and download flow without using a mouse
- **FR23:** The web UI populates all configuration options and gallery content from the REST API metadata endpoint
- **FR40:** A developer can access the tool through two distinct entry points: a direct configuration form (for developers who know what they want) and a project gallery (for discovery-oriented developers), with both leading to the same generation flow
- **FR41:** A developer can access an explanation distinguishing between available project types to inform their selection
- **FR43:** The web UI renders the project file tree preview from template manifests in the metadata response, without a per-change server round-trip

### REST API

- **FR24:** An API consumer can generate and download a project archive via `POST /api/v1/generate` with `Accept: application/zip`
- **FR25:** An API consumer can retrieve all supported configuration options and project template manifests via `GET /api/v1/metadata`
- **FR26:** An API consumer can access the complete OpenAPI specification at `/api/v1/docs`
- **FR27:** The system enforces a rate limit per IP address and returns a structured error response when exceeded

### CLI

- **FR28:** A developer can generate and download a project archive using `npx operaton-starter` with all options as command-line flags
- **FR29:** The CLI outputs the project archive as raw bytes to stdout when stdout is a pipe, enabling shell scripting
- **FR30:** A developer can instruct the CLI to extract the generated archive into a specified directory

### MCP Integration

- **FR31:** An AI assistant can generate an Operaton project archive by invoking the `generate_project` MCP tool from the `operaton-starter-mcp` npm package
- **FR32:** The `operaton-starter-mcp` package can be configured with a custom base URL to point at a self-hosted instance

### Generated Project Quality

- **FR33:** Every generated project includes a README with project-specific next-step instructions tailored to the selected project type and build system
- **FR34:** Every generated project includes a configured dependency update file (Dependabot or Renovate) ready to use without modification
- **FR35:** Generated Process Application projects include a GitHub Actions CI/CD workflow that passes on first push
- **FR36:** Generated projects with Docker Compose enabled include a `docker-compose.yml` that starts the application
- **FR44:** Generated Process Application projects include a `JavaDelegate` implementation stub wired to the skeleton BPMN service task and a JUnit test that deploys and executes the skeleton process end-to-end without modification

### Self-Hosting & Operations

- **FR37:** An operator can deploy Operaton Starter as a self-hosted instance using a Docker image with no external service dependencies at startup
- **FR38:** An operator can configure self-hosted instance defaults (default Group ID, Maven registry URL, Operaton version) via environment variables
- **FR39:** The running instance exposes a health check endpoint for operational monitoring

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
- **NFR14:** Generated projects using Gradle target Gradle 8+
- **NFR15:** The `operaton-starter-mcp` npm package supports all Node.js Active LTS versions
- **NFR16:** Browser support for the web UI covers the latest 2 major versions of Chrome, Firefox, Safari, and Edge
- **NFR20:** The web UI visual design is consistent with the `operaton.org` and `docs.operaton.org` design system — colors, typography, and component patterns signal the same product family

### Correctness

- **NFR17:** All supported project type × build system combinations (MVP: 6) are validated in CI on every template change; each combination is compiled and its tests executed in a CI matrix job; zero test failures are acceptable; any failure blocks merge

### Maintainability & Operability

- **NFR18:** The service emits structured JSON logs compatible with standard log aggregation tools
- **NFR19:** The Docker image is configurable entirely via environment variables; no file-based configuration is required at runtime

