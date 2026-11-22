---
stepsCompleted: ['step-01-validate-prerequisites', 'step-02-design-epics', 'step-03-create-stories', 'step-04-final-validation']
workflowStatus: complete
completedAt: '2026-06-08'
lastAmendedAt: '2026-06-13'
inputDocuments:
  - 'docs/bmad/planning-artifacts/prd.md'
  - 'docs/bmad/planning-artifacts/architecture.md'
  - 'docs/bmad/planning-artifacts/ux-design-specification.md'
  - 'docs/bmad/planning-artifacts/prds/prd-operaton-starter-uc-enhancements-2026-06-06/prd.md'
  - 'docs/bmad/planning-artifacts/prds/prd-operaton-starter-2026-06-08/prd.md'
  - 'docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/prd.md'
  - 'docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/addendum.md'
  - 'docs/bmad/planning-artifacts/ux-designs/ux-operaton-starter-2026-05-31/DESIGN.md'
  - 'docs/bmad/planning-artifacts/ux-designs/ux-operaton-starter-2026-05-31/EXPERIENCE.md'
amendments:
  - date: '2026-06-13'
    epic: 'Epic 8 — Examples Gallery'
project_name: operaton-starter
---

# operaton-starter - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for operaton-starter, decomposing the requirements from the main PRD, Architecture, and UX Design Specification into implementable stories.

---

## Requirements Inventory

### Functional Requirements

**Generation Engine**

FR1: The system generates a project archive for any supported project type × build system combination
FR2: Generated projects compile and pass their included tests without manual modification; they are complete working examples with meaningful process logic, valid BPMN, and implemented delegates
FR3: The system always targets the current stable Operaton release (no user-selectable version)
FR4: Developer identity (Group ID, Artifact ID, project name) propagates consistently across all generated files — Java packages, BPMN process IDs, Spring application.name
FR5: Generated BPMN files contain complete, graphically valid diagrams — all flow elements include BPMNShape and BPMNEdge layout data
FR6: Process Archive projects include a processes.xml deployment descriptor pre-configured with the selected deployment target
FR7: Process Archive projects generate target-platform-appropriate artifact configuration (WAR/JAR) matching the selected platform
FR8: The generation engine is a single shared implementation invoked by all channels — web UI, REST API, and CLI; no per-channel generation logic
FR42: CLI client code is generated from the OpenAPI specification; no hand-written client code exists independently of the API contract

**Project Configuration**

FR9: Project type selection (gallery card) is a prerequisite to configuration; project type is not selectable on the configuration form
FR10: Build system selection is a two-level choice — Maven or Gradle; if Gradle is chosen a DSL sub-option (Groovy/Kotlin) becomes visible and is required; DSL option is hidden for Maven
FR11: Developers can specify Group ID, Artifact ID, and project name as project identity
FR12: Process Archive projects expose a target-platform selector (Tomcat, Wildfly; list is extensible); selected platform determines artifact type and deployment descriptor pre-configuration
FR13: Dependency updates is an opt-in Extra (off by default); when enabled, a Dependabot/Renovate sub-option appears and is required; sub-option is hidden when the Extra is unchecked
FR14: Docker Compose generation is an opt-in Extra, shown only for Process Application projects (not Process Archive)
FR15: GitHub Actions CI/CD skeleton generation is an opt-in Extra
FR16: A developer can share a project configuration as a URL that restores and pre-fills the configuration form on load
FR56: All Extras (Dependency Updates, Docker Compose, GitHub Actions) are off by default; must be explicitly enabled

**Web UI**

FR17: A developer can configure a project and download a ZIP archive through a browser
FR18: A developer can browse project types as a visual gallery with capability descriptions, tags, and persona hints
FR19: Live file tree preview updates within 200ms of any configuration change; preview renders client-side from the metadata payload without server round-trips
FR20: Inline contextual help for any configuration option is available without leaving the page
FR22: Full configuration-and-download flow is completable without a mouse (keyboard-complete)
FR23: Web UI populates all configuration options and gallery content from the REST API metadata endpoint
FR40: Two distinct entry points on the landing page: direct configuration form (Practitioner) and project gallery (Explorer); both lead to the same generation flow; gallery landing page presents project types first, followed by use case examples
FR41: A developer can access an explanation distinguishing between available project types inline
FR43: Web UI renders the file tree preview from template manifests in the metadata response without a per-change server round-trip
FR45: Configuration details page requires a project type in route state; direct navigation without project type redirects to gallery; project type is displayed read-only on the details page
FR46: Configuration options are conditionally rendered based on the selected project type; options that do not apply are hidden entirely (not disabled)
FR57: Clicking a file in the File Structure Preview shows its content in an adjacent content pane; template placeholders are substituted client-side with current form values; content updates on every configuration change without a server round-trip
FR60: Web UI serves a favicon.ico derived from the Operaton logo; no 404 error is emitted for /favicon.ico requests
FR67: Gallery displays curated use case examples as a second section below project types; each example card shows title, one-sentence description, and capability tags; clicking a card pre-fills the configuration form
FR76: Content pane reactively re-renders when any configuration value changes while a file is open
FR77: Content pane clears immediately when a configuration change causes the open file to no longer appear in the file tree
FR78: Each use case example entry in the metadata response carries its own templateManifest enabling the File Structure Preview to show the actual use case workflow

**REST API**

FR24: API consumers can generate and download a project archive via POST /api/v1/generate with Accept: application/zip
FR25: API consumers can retrieve all supported configuration options and project template manifests via GET /api/v1/metadata
FR26: API consumers can access the complete OpenAPI specification and an interactive Scalar API documentation UI at /api/v1/docs
FR27: The system enforces a rate limit per IP address (10 req/min) and returns HTTP 429 + Retry-After header on breach

**CLI**

FR28: Developers can generate and download a project archive using `npx operaton-starter` with all options as command-line flags
FR29: CLI outputs raw bytes to stdout when stdout is a pipe, enabling shell scripting
FR30: CLI supports `--output <dir>` flag to extract the generated archive into a specified directory

~~### MCP Integration — removed; the MCP module (`operaton-starter-mcp` npm package) is out of scope. FR31 and FR32 retired.~~

**Generated Project Quality**

FR33: Every generated project includes a README with project-specific next-step instructions; all URLs and commands reflect actual project configuration; README includes chmod+x mvnw/gradlew instruction for Mac/Linux before first run command
FR34: When Dependency Updates is opted in, the generated project includes a configured Dependabot or Renovate file ready to use without modification
FR35: GitHub Actions CI/CD skeleton is included when opted in; passes CI on first push
FR36: Docker Compose projects include a docker-compose.yml and multi-stage Dockerfile when opted in
FR44: Generated projects include complete, runnable delegate implementations wired to BPMN service tasks — not stubs; a JUnit test deploys and executes the full process end-to-end
FR45b: Every generated project includes the appropriate build tool wrapper (mvnw/gradlew) with all required wrapper files
FR58: Generated projects have an elaborated, well-separated file structure — domain logic, process resources, configuration, and tests in distinct packages and directories
FR59: Every generated project compiles, all included tests pass, and the application starts without errors on first run; CI test matrix verifies all project-type × build-system combinations
FR75: Spring Boot Process Application projects include the Operaton banner.txt at src/main/resources/banner.txt displaying the Operaton ASCII logo with version info

**Use Case Examples**

FR67uc: Four MVP use case examples are available in the gallery and via all generation channels: UC-01 Leave Request, UC-02 Loan Application, UC-03 Incident Management, UC-04 Order Fulfillment
FR68: Each MVP use case example satisfies the self-containment invariant: `docker compose up -d` + `./mvnw spring-boot:run` starts successfully with no manual configuration; all included JUnit tests pass; integration test asserts process definition deployed before business-logic assertions
FR69: Each use case seeds user roles and groups via data.sql at startup; username=password for discoverability; an Operaton admin user is created at startup if it does not already exist
FR70: Each use case includes a docker-compose.yml with PostgreSQL as default datasource; WireMock included for examples that stub external APIs; all services include health checks; Spring Boot app depends on services with condition: service_healthy
FR71: Each use case includes a character-narrated "Getting Started in 5 Minutes" README section: names pre-seeded users, walks through Tasklist as those characters, includes Bootstrap Data section, includes BPMN process model image, includes chmod+x instruction before first run command
FR72: WireMock stub mapping files are committed in src/main/resources/wiremock/mappings/ and mounted via bind-mount in docker-compose.yml; no stubs configured in Java code; WireMock container image version is pinned
FR73: Use case examples are discoverable via GET /api/v1/metadata and generatable via POST /api/v1/generate using the useCaseId parameter; no separate generation path exists
FR74: Each use case includes an application-h2.properties Spring profile that switches the datasource to embedded H2 with no code changes; README documents the switch; H2 profile is active during mvn test

**Self-Hosting & Operations**

FR37: Operaton Starter is deployable as a self-hosted instance using a single Docker image that bundles the web UI, REST API, and generation engine; accessible on a single port with no external service dependencies at startup
FR38: Self-hosted instance defaults (Group ID, Maven registry URL, Operaton version) are configurable via environment variables
FR39: Running instance exposes a health check endpoint at /actuator/health
FR47: Repository contains a Dockerfile for building the Operaton Starter application image
~~FR48 — MCP self-hosting bridge: removed. Documented connection between the `operaton-starter-mcp` npm package and a self-hosted instance via `BASE_URL` no longer applies now that the MCP module is out of scope.~~

**Release & Distribution**

FR49: Releases are created via GitHub Actions using JReleaser; JReleaser creates the GitHub Release, generates changelog from conventional commits, and coordinates all distribution targets
FR50: Docker image published to Docker Hub as operaton/operaton-starter on every release; tags follow semver with a latest tag updated on each stable release
FR51: Maven artifacts published to Maven Central on every release via Sonatype OSSRH coordinated by JReleaser
~~FR52 — MCP npm publish: removed. The `operaton-starter-mcp` npm package is no longer published; there is nothing to publish now that the MCP module is out of scope.~~
FR53: Repository documentation specifies all required GitHub Actions secrets for the release workflow (Docker Hub, Maven Central, npm, GitHub token)

---

### Non-Functional Requirements

NFR1: POST /api/v1/generate responds with a complete ZIP within 1 second for up to 10 concurrent requests under normal load
NFR2: Web UI live preview updates within 200ms of any configuration change
NFR3: End-to-end time from UI landing to ZIP download completes within 30 seconds
NFR4: The starter's own repository is configured with Dependabot or Renovate for Operaton version bump automation
NFR5: Public instance at start.operaton.org achieves 99.9% uptime over a rolling 30-day window
NFR6: Self-hosted Docker image starts with no external network calls; failure of any external service does not prevent generation; no runtime database dependency
NFR7: All traffic to start.operaton.org is served over HTTPS; HTTP requests redirect to HTTPS
NFR8: No user-identifying data is persisted; only transient IP-based rate-limit data is held
NFR9: Rate limit enforcement returns HTTP 429 with Retry-After header
NFR10: Service is horizontally scalable; all instances are interchangeable; no sticky sessions required
NFR11: Web UI conforms to WCAG 2.1 Level AA; validated using automated accessibility tooling (axe-core) in CI and manual keyboard navigation testing
NFR12: All web UI functionality is operable via keyboard navigation with visible focus indicators throughout
NFR13: Generated Process Application projects target Java 21+; use Spring Boot version from current Operaton BOM
NFR14: Generated projects using Gradle target Gradle 8+; bundled Gradle wrapper targets current pinned Gradle version
~~NFR15 — MCP Node.js LTS support: removed along with the `operaton-starter-mcp` npm package.~~
NFR16: Browser support covers latest 2 major versions of Chrome, Firefox, Safari, and Edge
NFR17: All supported project type × build system combinations (MVP: 6) validated in CI on every template change; zero test failures acceptable; any failure blocks merge
NFR18: Service emits structured JSON logs compatible with standard log aggregation tools
NFR19: Docker image is configurable entirely via environment variables; no file-based configuration required at runtime
NFR20: Web UI visual design is consistent with the operaton.org and docs.operaton.org design system — colors, typography, and component patterns
NFR21: On any PR that modifies generation templates, a dedicated CI workflow generates projects for affected combinations, builds them, and starts the application; all steps must pass; unaffected combinations are excluded
NFR22: Each submodule (starter-server, starter-templates, starter-archetypes, starter-web) has its own README covering role, prerequisites, build-in-isolation, run-locally, and at least one usage example

---

### Additional Requirements (Architecture)

- Generation engine is a pure-Java in-process library (`starter-templates`); Maven subprocess MUST NOT be used at runtime (violates NFR1 ≤1s); Maven Archetype format is the template authoring standard only
- OpenAPI spec is the single source of truth for the API layer; spec must be frozen before CLI client generation begins; any post-freeze change requires regenerating all clients
- `GET /api/v1/metadata` is the projection contract between the engine and all consumers (web UI, CLI); schema defined before any channel implementation begins; includes projectTypes[], buildSystems[], globalOptions, and templateManifest per project type / use case
- No `globalOptions.javaVersions` — generated projects target Java 21 with no picker
- `GenerationClient` interface in `starter-archetypes`: calls `starter-templates` directly, in-process — no REST client, no network dependency (see Epic 9)
- Vue 3 + Vite for `starter-web`; Tailwind CSS v3 with custom theme extending operaton.org design tokens
- Design tokens extracted from `github.com/operaton/operaton.org` Jekyll source before starter-web implementation begins
- Rate limiting via Bucket4j in-memory per IP; no Redis; stateless constraint preserved
- Docker registry: `docker.io/operaton/operaton-starter`; published on every tagged release via CI
- Spec-freeze gate: GitHub Actions check posting to PR status panel (Phase 1: warning; Phase 2: hard block)
- Multi-language build: Maven for Java modules, npm for TypeScript modules; CI orchestrates both

---

### UX Design Requirements

UX-DR1: Tailwind CSS v3 with custom theme extending operaton.org design tokens; primary #184AEF, secondary #27F3E0, neutral palette; Inter (sans) and JetBrains Mono (code) fonts; JIT purge requires all Tailwind classes to be static strings
UX-DR2: App Shell with fixed 64px header (Operaton logo + "Starter" wordmark), route outlet, footer; matches operaton.org header height
UX-DR3: Gallery View layout: hero section with headline + dual CTAs ("Configure Now" / "Browse Use Cases") → Project Types grid (primary) → Use Case Examples grid (secondary); section order is mandatory — project types always precede use cases
UX-DR4: Gallery cards: rounded border, hover shadow + border-primary transition, tag badges using secondary color (#27F3E0/20), inline "?" accordion for project type disambiguation
UX-DR5: Configuration View layout: two-panel (form left min-w 420px, preview right flex-1) on ≥768px; single-column on <768px with preview below (collapsible); form uses fieldset + legend for semantic section grouping
UX-DR6: State management via Vue 3 composables only (no Vuex/Pinia): useMetadata(), useProjectForm(), useGenerate(); form state synced to URL query params for shareable URLs via initFromQuery()
UX-DR7: File tree condition evaluation as pure client-side function evaluating templateManifest[].condition equality expressions (e.g. "buildSystem == 'MAVEN'"); no server round-trip per condition check
UX-DR8: Focus rings using #184AEF outline (2px, offset 2px) applied globally via Tailwind base layer *:focus-visible; meets WCAG 2.1 AA contrast on white backgrounds
UX-DR9: Form defaults: groupId=com.example, artifactId=my-process-app, projectType=PROCESS_APPLICATION, buildSystem=MAVEN, dockerCompose=false, githubActions=true
UX-DR10: Content pane renders template placeholder strings (e.g. {{groupId}}, {{artifactId}}) substituted client-side with live form values on every configuration change; no stale content from removed files

---

### FR Coverage Map

| FR Group | Epic | Notes |
|----------|------|-------|
| FR1–8, FR42 | Epic 1 | Generation engine core |
| FR24–27 | Epic 1 | REST API surface |
| FR9–16, FR56 | Epic 2 | Project configuration model |
| FR33–36, FR44, FR45b, FR58–59, FR75 | Epic 2 | Generated project quality |
| FR49–53 | Epic 2 | Release & distribution pipeline |
| FR37–39, FR47–48 | Epic 2 | Self-hosting & operations |
| FR18–19, FR23, FR40–41, FR43, FR67, FR78 | Epic 3 | Web UI gallery & metadata-driven preview |
| FR17, FR20, FR22, FR45, FR46, FR57, FR60, FR76–77 | Epic 3 | Web UI configuration view & accessibility |
| FR68–74, FR67uc | Epic 4 | Use case examples — base setup |
| FR28–30 | Epic 5 | CLI tool |
| ~~FR31–32 | Epic 5 | MCP integration — removed, MCP module out of scope~~ |
| NFR11–12, UX-DR1–10 | Epic 3 | Web UI UX/design system implementation |
| UC-FR-1, UC-FR-2, UC-FR-7 | Epic 6 | Process start auth, task auth audit, status variables |
| UC-FR-3, UC-FR-3b, UC-FR-4, UC-FR-5 | Epic 6 | Email send events + Mailpit (UC-01 + UC-02) |
| UC-FR-6 | Epic 6 | Embedded task forms (UC-01) |
| UC-FR-8, UC-FR-13, UC-FR-14 | Epic 6 | Timer escalation, task-local vars, History API (UC-01) |
| UC-FR-10 | Epic 6 | Business keys (UC-02) |
| UC-FR-11 | Epic 6 | Signal escalation (UC-03) |
| UC-FR-9, UC-FR-12 | Epic 6 | Failure path/retry, suspension demo (UC-04) |
| UC-FR-16 | Epic 6 | BPMN swimlane layout + task renames (all use cases) |
| UC-DR-15 | Epic 6 | REST API curl examples (all use case READMEs) |

---

## Epic List

### Epic 1: Core Generation Engine & REST API
The generation server and pure-Java template engine are live; all channels can call POST /api/v1/generate and GET /api/v1/metadata; OpenAPI spec is frozen and forms the contract for all downstream channel clients. Stories must be sequenced: metadata schema → engine → REST API → rate limiting.
**FRs covered:** FR1–8, FR24–27, FR42

### Epic 2: Generated Project Quality, Release & Operations
Every generated project (any type × build system combination) compiles, tests pass, and starts on first run. The CI matrix enforces this as a merge gate. The self-hosted Docker image works, the release pipeline publishes to all targets, and submodule READMEs are complete.
**FRs covered:** FR9–16, FR33–36, FR44, FR45b, FR47–53, FR56, FR58–59, FR75; NFR4, NFR17, NFR21, NFR22

### Epic 3: Web UI — Gallery, Configuration & Live Preview
The Vue 3 SPA at start.operaton.org is live with gallery view, configuration form, client-side file tree preview, shareable URLs, keyboard-complete flow, WCAG 2.1 AA, and Operaton design token fidelity.
**FRs covered:** FR17–20, FR22–23, FR40–41, FR43, FR45–46, FR57, FR60, FR67, FR76–78; NFR2, NFR11–12, NFR16, NFR20; UX-DR1–10

### Epic 4: Use Case Examples
All four MVP use case examples (Leave Request, Loan Application, Incident Management, Order Fulfillment) are self-contained, out-of-the-box runnable, fully documented with character-narrated READMEs, and generatable via all channels.
**FRs covered:** FR67uc, FR68–74, FR78

### Epic 5: CLI Integration
`npx operaton-starter` is live, generated from the OpenAPI spec, and published to its registry. The channel exercises the same generation engine as the web UI.
**FRs covered:** FR28–30, FR42

### Epic 6: Use Case Enhancements — Authorization, Email, Advanced Patterns & Visual Clarity
The four use case examples are elevated from functional scaffolding to professional-grade teaching tools: role-based access enforced by the engine, email notifications via BPMN send events, swimlane layout communicating actor responsibilities, and advanced Operaton API patterns (timer escalation, signal escalation, payment failure/retry, business keys, task-local variables, History API). This epic depends on Epic 4 (base use cases must exist). Stories are sequenced: structural BPMN changes first (swimlanes), then authorization, then per-use-case feature enhancements.
**FRs covered (UC Enhancements PRD):** FR-1, FR-2, FR-3, FR-3b, FR-4, FR-5, FR-6, FR-7, FR-8, FR-9, FR-10, FR-11, FR-12, FR-13, FR-14, FR-16; DR-15

### Epic 9: Maven Archetype Integration
The `starter-archetypes` module provides `mvn archetype:generate` as a first-class generation channel, calling the shared generation engine (`starter-templates`) directly and in-process — no network dependency, no running server required. Added 2026-07-14 via sprint change proposal (see `sprint-change-proposal-2026-07-14.md`); no prior epic covered this module.
**FRs covered:** FR42

---

## Epic 1: Core Generation Engine & REST API

The generation server and pure-Java template engine are live; the OpenAPI spec is frozen and forms the contract for all downstream channel clients.

### Story 1.1: Define Metadata Schema and Freeze OpenAPI Spec

As a developer building operaton-starter,
I want the metadata schema and OpenAPI specification defined and frozen before any channel implementation begins,
So that the CLI and web UI all consume a stable, authoritative contract with no per-channel divergence.

**Acceptance Criteria:**

**Given** `GET /api/v1/metadata` is implemented
**When** the response is returned
**Then** it contains `projectTypes[]` (each with `id`, `displayName`, `description`, `tags`, `personaHint`, `templateManifest`), `buildSystems[]` (each with `id`, `displayName`, `dslOptions[]`), and no `globalOptions.javaVersions` field (Java 21 is hardcoded, no picker)

**Given** `GET /api/v1/metadata` is implemented
**When** a use case example is present in the metadata
**Then** each use case entry carries its own `templateManifest` listing the files specific to that use case (including the use-case-specific BPMN), enabling client-side file tree preview without a server round-trip

**Given** `openapi.yaml` is defined in the repository
**When** it is the source of truth for the API layer
**Then** no client code (CLI) is hand-written independently of the spec; a GitHub Actions check posts a warning to PR status if the spec has been modified without regenerating clients

**Given** the metadata schema is defined
**When** a developer calls `GET /api/v1/metadata`
**Then** the response time is under 200ms and the payload includes all configuration options required to render the full web UI without additional API calls

### Story 1.2: Implement Pure-Java Generation Engine (starter-templates)

As a developer building operaton-starter,
I want the generation engine to be a pure-Java library with no Spring context in the generation path,
So that it can be exercised in CI without a running application, responds within 1 second, and is embeddable in Maven archetypes.

**Acceptance Criteria:**

**Given** `starter-templates` is a standalone Java library
**When** it is invoked to generate a project
**Then** it runs entirely in-process — no Maven subprocess invocation, no external service calls, no Spring context required

**Given** the engine is given a `ProjectConfig` (groupId, artifactId, projectName, projectType, buildSystem)
**When** generation runs
**Then** Group ID, Artifact ID, and project name propagate consistently into Java package names, BPMN process IDs, and Spring application.name across all generated files

**Given** a Process Application project type is requested
**When** the BPMN template is rendered
**Then** the output BPMN file contains a complete graphically valid diagram — every flow element has BPMNShape and BPMNEdge layout data; the diagram renders correctly in any BPMN-aware tool without manual repositioning

**Given** a Process Archive project type is requested with a target platform
**When** the project is generated
**Then** `processes.xml` is present, pre-configured for the selected platform; artifact configuration (WAR/JAR) matches the platform

**Given** `starter-templates` is built in isolation from `starter-server`
**When** the CI matrix runs
**Then** `starter-templates` tests complete without any Spring Boot dependency on the classpath

### Story 1.3: Implement REST API — POST /api/v1/generate

As an API consumer,
I want to generate and download a project archive via POST /api/v1/generate,
So that any channel (web UI, CLI, curl) can use the same endpoint to produce identical output.

**Acceptance Criteria:**

**Given** a valid project configuration JSON is sent to `POST /api/v1/generate` with `Accept: application/zip`
**When** the request is processed
**Then** a ZIP archive is returned within 1 second for up to 10 concurrent requests; the ZIP contains a compilable, runnable project

**Given** an optional `useCaseId` parameter is included in the request body
**When** the server processes the request
**Then** it resolves the `useCaseId` to a fixed parameter bundle and generates using the standard engine — no separate generation path for use case examples

**Given** a single shared generation engine implementation (`starter-templates`)
**When** any channel calls the shared engine — web/CLI/curl via the API, archetype via a direct in-process call to the same engine (Epic 9)
**Then** the output is functionally identical across all channels — same files, same structure, same content (generation timestamps excepted)

**Given** the generated project is downloaded
**When** the developer runs `./mvnw spring-boot:run` (or `./gradlew bootRun`) without any manual modification
**Then** the Operaton engine starts successfully; the process definition is deployed; the project compiles and all included tests pass

### Story 1.4: Implement Rate Limiting and API Docs Endpoint

As an API consumer,
I want the API to enforce rate limits and expose its specification,
So that the service is protected against abuse and I can explore the API interactively.

**Acceptance Criteria:**

**Given** a single IP address sends more than 10 requests per minute to any endpoint
**When** the 11th request is received
**Then** the server returns HTTP 429 with a `Retry-After` header specifying the retry interval; Bucket4j in-memory rate limiting is used (no Redis; stateless constraint preserved)

**Given** `GET /api/v1/docs` is requested
**When** the response is returned
**Then** the complete OpenAPI 3.x specification is accessible; the interactive Scalar API documentation UI is rendered via static HTML loading Scalar from CDN — no Spring Boot version coupling

**Given** `GET /actuator/health` is requested
**When** the application is running normally
**Then** HTTP 200 is returned with a health payload suitable for load balancer and monitoring checks

---

## Epic 2: Generated Project Quality, Release & Operations

Every generated project compiles and starts on first run. The CI matrix enforces this. The Docker image, release pipeline, and submodule documentation are complete.

### Story 2.1: Implement Process Application Template with Quality Gate

As a developer evaluating Operaton,
I want every generated Process Application to compile, pass its tests, and start without modification,
So that the "it just works" guarantee is unconditional and proven by CI.

**Acceptance Criteria:**

**Given** a Process Application project is generated for any of the 6 MVP build combinations (Maven, Gradle Groovy, Gradle Kotlin for each project type)
**When** the generated project is compiled and tested
**Then** all included tests pass; the application starts via `./mvnw spring-boot:run` or `./gradlew bootRun` without errors

**Given** the generated project includes delegate implementations
**When** a developer reviews the code
**Then** delegates are complete, runnable implementations wired to BPMN service tasks — not stubs; a JUnit test deploys and executes the full process end-to-end

**Given** the generated project has a `src/` directory
**When** the structure is reviewed
**Then** domain logic, process resources, configuration, and tests are in distinct packages and directories; no application logic is in a default or root package

**Given** a Spring Boot Process Application is generated
**When** the application starts
**Then** `src/main/resources/banner.txt` contains the Operaton ASCII logo sourced from the upstream Operaton Spring Boot Starter banner

**Given** a CI matrix job runs on every template change
**When** the matrix executes
**Then** all 6 project-type × build-system combinations are generated, compiled, and started; any failure blocks merge; a targeted PR-level workflow runs only the affected combinations

### Story 2.2: Implement Generated Project Extras

As a developer,
I want Extras (Docker Compose, GitHub Actions, Dependency Updates) to be included only when explicitly opted in,
So that generated projects contain no unexpected files and all Extras work correctly out of the box.

**Acceptance Criteria:**

**Given** a developer opts in to Docker Compose
**When** the project is generated
**Then** `docker-compose.yml` and a multi-stage `Dockerfile` (Maven build + runtime image) are present; docker compose up starts the application

**Given** a developer opts in to GitHub Actions
**When** the project is generated
**Then** a `.github/workflows/ci.yml` is present; the workflow passes on first push to a new repository

**Given** a developer opts in to Dependency Updates with Renovate
**When** the project is generated
**Then** a `renovate.json` is present, configured and ready to use without modification

**Given** a developer opts in to Dependency Updates with Dependabot
**When** the project is generated
**Then** `.github/dependabot.yml` is present, configured and ready to use without modification

**Given** no Extras are selected (default state)
**When** the project is generated
**Then** none of the Extra files (Dockerfile, docker-compose.yml, CI workflow, dependency update config) are present in the ZIP

**Given** a developer selects Docker Compose
**When** the project type is Process Archive
**Then** the Docker Compose option is not shown at all (hidden, not disabled) — this Extra applies only to Process Application

### Story 2.3: Implement Build Tool Wrappers and README Quality

As a developer,
I want every generated project to include the correct build tool wrapper and a project-specific README,
So that the project builds without a globally installed Maven or Gradle and the README gives accurate, configuration-aware instructions.

**Acceptance Criteria:**

**Given** a Maven project is generated
**When** the ZIP is extracted
**Then** `mvnw`, `mvnw.cmd`, and `.mvn/wrapper/maven-wrapper.properties` are present; `./mvnw spring-boot:run` starts the application without a globally installed Maven

**Given** a Gradle project is generated
**When** the ZIP is extracted
**Then** `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.{jar,properties}` are present targeting Gradle 8+

**Given** any generated project's README is read
**When** the developer follows the instructions
**Then** the Cockpit URL reflects the actual configured server port; docker compose launch steps are present only when Docker Compose was enabled; a `chmod +x mvnw` (or `gradlew`) instruction appears on Mac/Linux immediately before the first run command; all next-step URLs are accurate to the project's actual configuration

**Given** the Troubleshooting section of the README is read
**When** a developer encounters a port conflict or datasource failure at first start
**Then** the README covers the most common startup failure modes with resolutions referencing the actual configured port

### Story 2.4: Self-Hosted Docker Image

As an operator,
I want to self-host Operaton Starter as a single Docker image,
So that my team can run a private instance with enterprise defaults and no external dependencies.

**Acceptance Criteria:**

**Given** the Docker image is started with `docker run operaton/operaton-starter`
**When** the container is running
**Then** the web UI, REST API, and generation engine are all accessible on a single port; no external network calls are made at startup; generation works without any external service dependency

**Given** environment variables `DEFAULT_GROUP_ID`, `MAVEN_REGISTRY`, and `OPERATON_VERSION` are set
**When** the container starts
**Then** the self-hosted instance applies these as defaults; generated projects reflect the configured values

**Given** `/actuator/health` is polled
**When** the container is healthy
**Then** HTTP 200 is returned; this endpoint is usable as a Docker health check and load balancer probe

**Given** the repository `Dockerfile` is built
**When** the build sequence is followed (Maven build → Docker image build from pre-built JAR)
**Then** the Docker image build requires no Maven or internet access; it is fully self-contained once the JAR is present

### Story 2.5: Release Pipeline with JReleaser

As a maintainer,
I want every release to publish to Docker Hub, Maven Central, and npm in a single automated run,
So that all distribution targets are always version-aligned and no manual steps are required.

**Acceptance Criteria:**

**Given** a version tag is pushed to the repository
**When** the GitHub Actions release workflow runs
**Then** JReleaser creates the GitHub Release with a changelog generated from conventional commits; the Docker image is published to `docker.io/operaton/operaton-starter` with the semver tag and updated `latest`; Maven artifacts are published to Maven Central via Sonatype OSSRH; the `operaton-starter` CLI npm package is published to npmjs.com at the same version

**Given** the repository documentation
**When** a maintainer prepares for a first release
**Then** all required GitHub Actions secrets are documented: Docker Hub credentials, Maven Central/Sonatype credentials, npm publish token, GitHub token required by JReleaser

**Given** the starter repo itself
**When** a new Operaton stable release is published
**Then** Dependabot or Renovate opens a PR updating the Operaton version dependency; the CI matrix on that PR verifies all 6 combinations pass before merge

### Story 2.6: Submodule Documentation

As a contributor,
I want each submodule to have a standalone README,
So that I can build and exercise any part of the monorepo using only its README without consulting other sources.

**Acceptance Criteria:**

**Given** any submodule README (`starter-server`, `starter-templates`, `starter-archetypes`, `starter-cli`, `starter-web`)
**When** a contributor follows only that README
**Then** they can: understand the submodule's role in the overall system, install prerequisites, build the submodule in isolation, run or use it locally, and complete at least one concrete usage example without consulting sibling READMEs

---

## Epic 3: Web UI — Gallery, Configuration & Live Preview

The Vue 3 SPA is live with the gallery view, configuration form, client-side live preview, shareable URLs, keyboard-complete flow, WCAG 2.1 AA compliance, and Operaton design token fidelity.

### Story 3.1: Configure Tailwind Design System with Operaton Tokens

As a developer building the web UI,
I want the Tailwind CSS configuration to use Operaton design tokens before any component is built,
So that all UI components are visually consistent with operaton.org from day one.

**Acceptance Criteria:**

**Given** `starter-web/tailwind.config.js` is configured
**When** any UI component is built
**Then** the theme includes: primary `#184AEF`, primary-dark `#0a2dbf`, secondary `#27F3E0`, neutral palette (0/50/200/500/900), Inter sans font, JetBrains Mono mono font, spacing tokens (xs/s/m/l/xl), borderRadius-s `0.5em`, maxWidth-content `80rem`

**Given** the global CSS base layer is configured
**When** any focusable element receives keyboard focus
**Then** a 2px `#184AEF` outline with 2px offset is visible (via `*:focus-visible`); this focus ring meets WCAG 2.1 AA contrast requirements against white backgrounds

**Given** all Tailwind classes are applied
**When** the JIT build runs
**Then** all Tailwind utility classes used in templates are static strings; no dynamic string concatenation creates class names that would be purged

### Story 3.2: Implement App Shell and Gallery View

As a developer evaluating Operaton,
I want to see a professional gallery landing page with project types and use case examples,
So that I can discover the right starting point for my project without consulting external documentation.

**Acceptance Criteria:**

**Given** a developer arrives at `/`
**When** the page renders
**Then** a fixed 64px header with the Operaton logo and "Starter" wordmark is visible; a hero section with "Configure Now →" and "Browse Use Cases ↓" CTAs is present; the Project Types section (primary) appears before the Use Case Examples section (secondary); page content is max-width 80rem, centered

**Given** the gallery fetches metadata from `GET /api/v1/metadata`
**When** the response arrives
**Then** all project type cards and use case example cards are rendered from metadata — no hardcoded option lists exist in any component

**Given** a project type card is hovered
**When** the cursor moves over it
**Then** the card shows `shadow-md` and `border-primary` transition; the "?" icon reveals an inline accordion explaining the project type without navigating away

**Given** a project type card's [Configure →] button is clicked
**When** navigation occurs
**Then** the developer is taken to `/configure?projectType={selectedType}` with the project type carried in route state

**Given** a use case example card is clicked
**When** navigation occurs
**Then** the developer is taken to `/configure` with the use case's pre-filled parameter bundle applied to the form

**Given** `/` is loaded
**When** the page is navigated using only Tab, Enter, and arrow keys
**Then** every card, button, and accordion is reachable and operable; visible focus indicators are present throughout

### Story 3.3: Implement Configuration Form with Conditional Options

As a developer configuring a project,
I want a form that shows only the options relevant to my selected project type and hides inapplicable ones,
So that I am never presented with options that would generate invalid or irrelevant output.

**Acceptance Criteria:**

**Given** a developer arrives at `/configure?projectType=PROCESS_APPLICATION`
**When** the form renders
**Then** the project type is displayed as a read-only badge (not an editable field); Group ID defaults to `com.example`; Artifact ID defaults to `my-process-app`; all Extras are unchecked by default

**Given** a developer arrives at `/configure?projectType=PROCESS_ARCHIVE`
**When** the form renders
**Then** the Docker Compose Extra is hidden entirely (not shown as disabled); a Target Platform selector (Tomcat, Wildfly) is visible

**Given** the Gradle build system is selected
**When** the build system changes
**Then** a DSL sub-option (Groovy / Kotlin) appears and is required before generation is enabled; selecting Maven hides the DSL sub-option

**Given** the Dependency Updates Extra is checked
**When** the checkbox is toggled
**Then** a Dependabot/Renovate sub-option appears and is required before generation is enabled; unchecking the Extra hides the sub-option immediately

**Given** a developer navigates directly to `/configure` without a project type in the route
**When** the page loads
**Then** they are redirected to the gallery at `/` to make a project type selection first

**Given** the form is filled out
**When** the developer copies the URL from the browser address bar
**Then** pasting that URL in a new tab restores the full form state including project type, identity fields, build system, and selected Extras

### Story 3.4: Implement Live File Tree Preview and Content Pane

As a developer configuring a project,
I want to see the exact file structure and file contents of my project update in real time as I configure,
So that I know precisely what I will download before clicking Generate.

**Acceptance Criteria:**

**Given** any configuration value changes (groupId, artifactId, buildSystem, Extras)
**When** the change is registered
**Then** the file tree preview updates within 200ms; no server round-trip is made; the update is a pure client-side computation from `templateManifest` conditions in the metadata response

**Given** a developer clicks a file in the File Structure Preview
**When** the file is selected
**Then** its content is shown in an adjacent content pane; template placeholders (e.g. `{{groupId}}`, `{{artifactId}}`) are substituted with the current form values client-side

**Given** a file is open in the content pane and the developer changes any configuration value
**When** the change is registered
**Then** the content pane re-renders immediately with the updated substituted values; no file re-selection is required

**Given** a file is open in the content pane and a configuration change removes that file from the tree (e.g. switching Maven → Gradle removes pom.xml)
**When** the change is registered
**Then** the content pane clears immediately and shows an empty state; no stale content from the removed file remains visible

**Given** a use case example is selected from the gallery
**When** the configuration view loads with that use case's parameter bundle
**Then** the file tree shows the use case-specific files (including the use case BPMN) from that use case's own `templateManifest` — not the generic skeleton process

### Story 3.5: Implement Generate & Download, Favicon, and Accessibility Audit

As a developer,
I want to download my configured project with a single click, with the tool meeting all accessibility requirements,
So that the experience is fast, inclusive, and polished.

**Acceptance Criteria:**

**Given** the form has a valid configuration
**When** the developer clicks [Generate & Download]
**Then** `POST /api/v1/generate` is called; the ZIP downloads as `{artifactId}.zip`; the total time from clicking to ZIP download completes in under 30 seconds

**Given** a developer visits `start.operaton.org` in any supported browser
**When** the page loads
**Then** `favicon.ico` derived from the Operaton logo is served from `/favicon.ico`; no 404 is emitted

**Given** the complete UI is audited with axe-core
**When** the audit runs in CI
**Then** zero WCAG 2.1 AA violations are reported; the audit passes as part of every PR merge gate

**Given** a developer uses only a keyboard to complete the full flow (gallery → type selection → configure → download)
**When** they navigate using Tab, Enter, Space, and arrow keys
**Then** every interactive element is reachable; focus indicators are visible throughout; the flow completes successfully

**Given** the UI is tested in the latest 2 versions of Chrome, Firefox, Safari, and Edge
**When** each browser is used
**Then** all features work correctly; no browser-specific failures are present

---

## Epic 4: Use Case Examples

All four MVP use case examples are self-contained, out-of-the-box runnable, fully documented, and generatable via all channels.

### Story 4.1: UC-01 Leave Request — Base Implementation

As a developer evaluating Operaton,
I want the Leave Request use case to run out of the box with Postgres, seed data, and a character-narrated README,
So that I can explore user tasks, candidate groups, and happy/rejection paths without any setup friction.

**Acceptance Criteria:**

**Given** the UC-01 project is generated and `docker compose up -d` is run
**When** `./mvnw spring-boot:run` starts
**Then** the application connects to PostgreSQL; the Leave Request process definition is deployed; the integration test asserts the process definition is reachable before any business-logic assertions

**Given** `data.sql` is executed at startup
**When** the Operaton identity service is queried
**Then** users `alice/alice` (employee), `bob/bob` (manager), `carol/carol` (HR) and their respective groups exist; the Operaton admin user exists and can log into Cockpit

**Given** the UC-01 docker-compose.yml
**When** `docker compose up -d` is run
**Then** a PostgreSQL service with a health check is started; the Spring Boot app depends on it with `condition: service_healthy`; an `application-h2.properties` profile exists and switches the datasource to H2 with `./mvnw spring-boot:run --spring.profiles.active=h2`; the H2 profile is active during `mvn test`

**Given** the UC-01 README is read
**When** a developer follows the Getting Started section
**Then** the README names alice, bob, and carol by role; walks the developer through starting a leave request as alice and approving as bob in Tasklist; includes a Bootstrap Data section describing each data.sql seed entry; includes a BPMN process model image; includes `chmod +x mvnw` before the first run command

### Story 4.2: UC-02 Loan Application — Base Implementation

As a developer evaluating Operaton,
I want the Loan Application use case to run out of the box with DMN decision evaluation and WireMock,
So that I can explore DMN business rules alongside BPMN, service task HTTP integration, and decision-driven branching.

**Acceptance Criteria:**

**Given** the UC-02 project is generated and `docker compose up -d` is run
**When** `./mvnw spring-boot:run` starts
**Then** the application connects to PostgreSQL; WireMock serves stubs from `src/main/resources/wiremock/mappings/`; the Loan Application process is deployed

**Given** WireMock stub mapping files
**When** they are reviewed
**Then** they are committed in `src/main/resources/wiremock/mappings/`; mounted into the WireMock container via bind-mount in docker-compose.yml; no stubs are configured in Java code; the WireMock container image version is pinned to a specific minor version

**Given** the DMN file `risk-assessment.dmn`
**When** the process reaches the BusinessRuleTask
**Then** it evaluates creditScore and loanAmount → riskLevel (low/medium/high) with hit policy FIRST; the DMN engine dependency is explicitly declared in the project (not assumed from transitive resolution)

**Given** the UC-02 README is read
**When** a developer follows it
**Then** it names jack (underwriter) and kate (applicant); walks through each risk path; includes character-narrated Getting Started, Bootstrap Data section, BPMN image, and chmod+x instruction

### Story 4.3: UC-03 Incident Management — Base Implementation

As a developer evaluating Operaton,
I want the Incident Management use case to run with a boundary timer event and test-profile-controlled time advancement,
So that I can explore SLA escalation and timer boundary events without wall-clock waiting in tests.

**Acceptance Criteria:**

**Given** the UC-03 project is generated and started
**When** the boundary timer event on the triage task fires
**Then** it escalates to the second-line engineer path without interrupting the original triage task

**Given** the integration test activates the test profile
**When** `mvn test` runs
**Then** the boundary timer is controlled via `ClockUtil`; `ClockUtil.reset()` is called after each timer-dependent test to prevent cross-test contamination; no wall-clock sleep is used

**Given** the UC-03 docker-compose.yml
**When** started
**Then** PostgreSQL and WireMock services include health checks; WireMock stubs cover close-ticket and notify-postmortem REST calls; the Spring Boot app depends on both with `condition: service_healthy`

**Given** the UC-03 README
**When** a developer follows it
**Then** it names henry (first-line) and iris (second-line); walks through the escalation scenario; includes Bootstrap Data section, BPMN image, and timer test profile explanation

### Story 4.4: UC-04 Order Fulfillment — Base Implementation

As a developer evaluating Operaton,
I want the Order Fulfillment use case to run with multi-step service task orchestration and WireMock stubs for inventory, payment, and notification,
So that I can explore mixed service/human task processes and conditional routing on external API results.

**Acceptance Criteria:**

**Given** the UC-04 project is generated and started
**When** the process runs the in-stock path
**Then** it executes inventory check → payment charge → Pack & Ship user task (dave/warehouse) → customer notification in sequence

**Given** `docker compose up -d` is run for UC-04
**When** WireMock starts
**Then** the test waits for `/__admin/mappings` health check before the first service task invocation; stubs cover inventory, payment, and customer notification endpoints; the WireMock image version is pinned

**Given** the UC-04 README
**When** a developer follows it
**Then** it names dave (warehouse) and walks through the fulfillment flow; includes Bootstrap Data section, BPMN image, out-of-stock path explanation, and chmod+x instruction

### Story 4.5: Use Case Example Gallery Integration and Multi-Channel Discoverability

As a developer,
I want all four use case examples to be discoverable via the gallery and generatable via any channel (web, REST, CLI),
So that the examples are first-class citizens of the generation platform, not just local templates.

**Acceptance Criteria:**

**Given** `GET /api/v1/metadata` is called
**When** the response is parsed
**Then** all four use case examples appear with `useCaseId`, `displayName`, description, capability tags, pre-filled parameter bundle, and their own `templateManifest` listing use-case-specific files

**Given** `POST /api/v1/generate` is called with `useCaseId: "leave-request"` (or any valid use case ID)
**When** the server processes the request
**Then** it resolves the use case to its parameter bundle and generates using the same standard engine — no separate generation path

**Given** the CLI is invoked with a use case flag
**When** the command runs
**Then** the correct use case project is generated and downloadable; the CLI is generated from the OpenAPI spec and exercises the same REST endpoint as the web UI

---

## Epic 5: CLI Integration

`npx operaton-starter` is live, generated from the OpenAPI spec, and published to its registry.

### Story 5.1: Implement CLI (npx operaton-starter)

As a developer,
I want to generate an Operaton project from the command line with all options as flags,
So that I can script project generation in CI, shell scripts, or automated workflows.

**Acceptance Criteria:**

**Given** `npx operaton-starter --projectType process-application --buildSystem maven --groupId com.example --artifactId my-app` is run in a pipe context (stdout is not a terminal)
**When** the command executes
**Then** raw ZIP bytes are written to stdout; no interactive prompts appear; the output is pipeable to `> my-app.zip` or `| unzip -d my-app -`

**Given** `npx operaton-starter --output ./my-project` is run
**When** the command executes
**Then** the generated ZIP is extracted into `./my-project/` without requiring a separate unzip step

**Given** the CLI source code
**When** it is reviewed
**Then** all client code is generated from `openapi.yaml` via the OpenAPI Generator tool; no hand-written HTTP client code exists outside the generated client

**Given** the CLI package is published to npm
**When** `npm install -g operaton-starter` is run on any Node.js Active LTS version
**Then** the CLI installs and `operaton-starter --help` shows all available flags

---

## Epic 6: Use Case Enhancements — Authorization, Email, Advanced Patterns & Visual Clarity

The four use case examples are elevated from functional scaffolding to professional-grade teaching tools. This epic depends on Epic 4. Story sequencing: swimlanes first (structural BPMN foundation) → authorization → per-use-case feature enhancements → REST documentation.

### Story 6.1: Restructure All Use Case BPMNs with Swimlane Pools and Rename Tasks

As a developer evaluating Operaton,
I want every use case BPMN to use collaboration pools with named swimlanes per actor group,
So that the diagram immediately communicates who does what without requiring me to read task labels or check candidateGroups attributes.

**Acceptance Criteria:**

**Given** any of the four use case BPMN files is opened in Operaton Modeler or Cockpit
**When** the diagram renders
**Then** the process is wrapped in a collaboration with a single pool; each lane is named after its actor group (`employees`, `managers`, `hr`, `underwriters`, `first-line`, `second-line`, `warehouse`, `sales`); a `System` lane contains all automated elements (service tasks, send tasks, gateways, timer events)

**Given** the BPMN XML is reviewed
**When** every flow element is checked
**Then** each element is assigned to the correct lane; no task label contains the responsible actor's name; sequence flows do not cross lane boundaries where the lane assignment already makes the actor clear

**Given** the task rename table from the UC Enhancements PRD is applied
**When** the BPMNs are updated
**Then** task names are: "Review Request" (was "Manager Reviews Request"), "Record Approved Leave" (was "HR Records Approved Leave"), "Triage" (was "First-Line Triage"), "Handle Escalation" (was "Second-Line Engineer"); "Employee Notified of Rejection" is absent (replaced by send event in Story 6.3)

**Given** all four BPMNs are updated atomically in the same commit
**When** the commit is reviewed
**Then** swimlane restructuring and task renames land together — no intermediate state exists where lanes are present but actor names remain in task labels, or vice versa

### Story 6.2: Add Process Start Authorization and Status Variables to All Use Cases

As a developer evaluating Operaton,
I want each use case process to be startable only by the designated user role, with a readable status variable visible in Cockpit,
So that I understand how `candidateStarterGroups` enforces role-based access and how to expose process state without opening the BPMN.

**Acceptance Criteria:**

**Given** each use case BPMN `<process>` element
**When** it is reviewed
**Then** `operaton:candidateStarterGroups` is declared with the correct value: `employees` for UC-01, UC-02, UC-03; `sales` for UC-04

**Given** UC-04's DataInitializer
**When** the application starts
**Then** group `sales` is created and user `frank` (frank@example.com, password `frank`) is created and assigned to `sales`

**Given** a user not in the designated starter group (e.g. bob attempting to start the loan application)
**When** they browse Tasklist
**Then** the process is not listed as startable for that user

**Given** each use case reaches a key state transition
**When** `execution.setVariable()` is called in the appropriate delegate or listener
**Then** the status variable is set: `leaveStatus` (`PENDING`→`APPROVED`/`REJECTED`), `loanDecision` (`PENDING`→`APPROVED`/`REJECTED`), `incidentPriority` (`LOW`→`HIGH` on escalation), `orderStatus` (`RECEIVED`→`FULFILLED`/`FAILED`)

**Given** an integration test for any one use case
**When** the process reaches its terminal state
**Then** the test asserts the status variable holds the expected final value via `runtimeService.getVariable()`

**Given** each use case README
**When** the Getting Started section is read
**Then** it documents which user to log in as to start the process (alice for UC-01/02/03, frank for UC-04)

**Given** all four BPMNs are audited for `operaton:candidateGroups` on every `<userTask>`
**When** the audit completes
**Then** all tasks carry the correct groups: `managers` on Review Request, `hr` on Record Approved Leave, `underwriters` on Underwriter Review, `first-line` on Triage, `second-line` on Handle Escalation, `warehouse` on Pack & Ship; any gap found must be filled before the story is marked complete

### Story 6.3: UC-01 Email Rejection via Send Event and Embedded Task Forms

As a developer evaluating Operaton,
I want the UC-01 rejection path to send an email via a BPMN send event and the manager/HR task forms to display all relevant leave data,
So that I see how Operaton models external notification as a process element and how process variables drive embedded task form content.

**Acceptance Criteria:**

**Given** `leave-request.bpmn.jte` on the rejection path
**When** the BPMN is reviewed
**Then** the "Employee Notified of Rejection" user task is absent; a Send Task named "Send Rejection Email" using delegate expression `${leaveRejectionEmailDelegate}` is present in the System lane

**Given** `LeaveRejectionEmailDelegate.java.jte` is implemented
**When** the Send Task executes
**Then** it resolves the requester's email from the identity service using the `requester` variable (set via `operaton:initiator`); sends a plain-text email with subject "Leave Request — Rejected" containing the requester name and requested dates; uses Spring `JavaMailSender`

**Given** `application.properties.jte` and `application-docker.properties.jte` for UC-01
**When** Spring Mail is configured
**Then** `spring.mail.host=localhost` and `spring.mail.port=1025` are present

**Given** the UC-01 `docker-compose.yml.jte`
**When** a Mailpit service is added
**Then** it uses image `axllent/mailpit:latest`, exposes SMTP on port 1025 and web UI on port 8025, and includes `restart: unless-stopped`

**Given** a leave request is started with `startDate`, `endDate`, and requester identity
**When** the process start listener or service task runs
**Then** process variables `startDate` (ISO-8601 String), `endDate` (ISO-8601 String), `days` (int, calendar days inclusive), and `remainingVacationDays` (int, from `VacationBalanceService` before any deduction) are set

**Given** the "Review Request" user task
**When** a manager opens it in Operaton Tasklist
**Then** an embedded HTML form (referenced via `operaton:formKey`) displays five read-only fields: Requester, Start Date, End Date, Days Requested, Remaining Vacation Days

**Given** the "Record Approved Leave" user task
**When** HR opens it
**Then** the same five read-only fields appear in an embedded HTML form; both forms are new files with no pre-existing `operaton:formKey` in the base BPMN

**Given** the UC-01 README
**When** the "Email Testing with Mailpit" section is read
**Then** it states the web UI URL `http://localhost:8025`, lists all UC-01 sample user email addresses, and explains that a rejected leave request triggers an email to the requester

### Story 6.4: UC-01 Timer Escalation, Task-Local Variables, and History API

As a developer evaluating Operaton,
I want to see non-responding manager tasks escalate via a timer, approval comments stored as task-local variables, and leave balances audited via the History API,
So that I understand non-interrupting timer boundary events, variable scoping, and process auditing patterns.

**Acceptance Criteria:**

**Given** the "Review Request" user task in `leave-request.bpmn.jte`
**When** a non-interrupting timer boundary event is added
**Then** the timer duration defaults to `PT72H` and is overridable via process variable `managerReviewTimeout` at process start

**Given** the timer fires
**When** the escalation path executes
**Then** a reminder service task delegate runs and sets process variable `escalated = true`; the Review Request task remains active and claimable by the manager

**Given** `LeaveRequestIT` sets `managerReviewTimeout = PT1S` at process start
**When** the test executes the timer job via `managementService.executeJob(jobId)`
**Then** the test asserts `escalated = true` is set and the Review Request task is still active

**Given** the manager completes "Review Request"
**When** `taskService.setVariableLocal(taskId, "approvalComment", comment)` is called before `taskService.complete(taskId)`
**Then** `approvalComment` is stored scoped to that task instance only and does not appear as a process-level variable

**Given** `LeaveRequestIT` completes the manager task with a comment
**When** `historyService.createHistoricVariableInstanceQuery().taskIdIn(taskId).list()` is called
**Then** the query returns `approvalComment` with the value that was set

**Given** `LeaveRequestIT` runs the full approved path through HR completion
**When** `historyService.createHistoricVariableInstanceQuery().variableName("remainingVacationDays").singleResult()` is called after the `VacationBalanceService` deduction
**Then** the historic balance recorded at process start is greater than the balance after deduction

### Story 6.5: UC-02 Email Rejection, Mailpit, and Business Keys

As a developer evaluating Operaton,
I want UC-02 loan rejections to send an email via a BPMN Send Task, with Mailpit for local email testing and a business key correlating the loan application to an external ID,
So that I see how Operaton integrates with external services and how business keys link process instances to real-world identifiers.

**Acceptance Criteria:**

**Given** `loan-application.bpmn.jte` on the high-risk rejection path
**When** it is reviewed
**Then** the `Auto-Reject Notify` service task is replaced with a Send Task named "Send Rejection Email" using delegate expression `${rejectionEmailDelegate}`

**Given** `RejectionEmailDelegate.java.jte` is implemented
**When** the Send Task executes
**Then** it reads `applicantEmail` from process variables; sends a plain-text email with subject "Loan Application — Decision" containing applicant name and reason; uses Spring `JavaMailSender`

**Given** UC-02 `DataInitializer.java.jte`
**When** a loan application process is started
**Then** `applicantEmail` is available as a process variable (populated from the start form or resolved from the identity service)

**Given** the UC-02 `docker-compose.yml.jte`
**When** Mailpit is added
**Then** it uses `axllent/mailpit:latest`, exposes SMTP on port 1025 and web UI on port 8025, with `restart: unless-stopped`

**Given** `LoanApplicationIT` starts a loan application
**When** `runtimeService.startProcessInstanceByKey(key, businessKey, variables)` is called
**Then** the business key follows the pattern `"LOAN-" + UUID` and is stored on the process instance

**Given** a running loan application process instance
**When** `runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult()` is called
**Then** the correct process instance is returned

**Given** the UC-02 README
**When** the "Email Testing with Mailpit" and business key sections are read
**Then** the email section states the web UI URL `http://localhost:8025` and lists sample user email addresses; the business key section explains what business keys are, why they matter for external correlation, and shows the query API example

### Story 6.6: UC-03 Signal Escalation and UC-04 Failure Path, Retry, and Suspension

As a developer evaluating Operaton,
I want UC-03 to demonstrate signal-based parallel escalation and UC-04 to demonstrate configurable error paths, job retry, and process suspension/activation,
So that I understand boundary signal events, BPMN error handling, retry policies, and the process suspension API.

**Acceptance Criteria:**

**Given** `incident-management.bpmn.jte`
**When** a non-interrupting boundary signal catch event named `EscalationSignal` is added to the "Triage" user task
**Then** the signal event wires to a sequence flow creating the "Handle Escalation" user task in parallel; the Triage task remains active

**Given** `IncidentManagementIT` sends `runtimeService.signalEventReceived("EscalationSignal")` after process start
**When** the signal is received
**Then** `taskService.createTaskQuery().taskDefinitionKey("Task_HandleEscalation").count()` returns 1
**And** `taskService.createTaskQuery().taskDefinitionKey("Task_Triage").count()` returns 1
**And** `execution.setVariable("incidentPriority", "HIGH")` was called on the escalation path

**Given** `PaymentDelegate.java.jte` for UC-04
**When** process variable `simulatePaymentFailure = true` is set at process start
**Then** the delegate throws `new BpmnError("PAYMENT_FAILED")` instead of completing normally

**Given** `order-fulfillment.bpmn.jte`
**When** a boundary error event catching `PAYMENT_FAILED` is added to the payment service task
**Then** the error path routes to a "Notify Customer of Failure" end event; the payment task declares `operaton:failedJobRetryTimeCycle="R3/PT10S"`

**Given** `OrderFulfillmentIT` starts a process with `simulatePaymentFailure = true`
**When** the test asserts terminal state
**Then** the process reaches the "Notify Customer of Failure" end event and `orderStatus` equals `FAILED`

**Given** `OrderFulfillmentIT` starts a process instance and calls `runtimeService.suspendProcessInstanceById(id)`
**When** suspension is applied
**Then** `runtimeService.createProcessInstanceQuery().suspended().processInstanceId(id).count()` returns 1; attempting `managementService.executeJob(jobId)` throws `SuspendedJobException`

**Given** `runtimeService.activateProcessInstanceById(id)` is called
**When** the process resumes
**Then** the process instance is no longer suspended and continues to completion

### Story 6.7: REST API Documentation in All Use Case READMEs

As a developer evaluating Operaton,
I want each use case README to include working curl examples for starting a process, listing tasks, and completing a task,
So that I can drive the full process flow from the command line without the Tasklist UI.

**Acceptance Criteria:**

**Given** the UC-01 `README.md.jte`
**When** the "REST API" section is read
**Then** it contains curl examples for: `POST /engine-rest/process-definition/key/leave-request/start` with a realistic variable payload (startDate, endDate, days); `GET /engine-rest/task?processDefinitionKey=leave-request`; `POST /engine-rest/task/{id}/complete` with an approval decision variable; examples use alice's credentials and the correct process key

**Given** the UC-02 `README.md.jte`
**When** the "REST API" section is read
**Then** it contains curl examples for: start (including `applicantEmail`); list tasks; complete the Underwriter Review task

**Given** the UC-03 `README.md.jte`
**When** the "REST API" section is read
**Then** it contains curl examples for: start; list tasks; `POST /engine-rest/signal` with body `{"name": "EscalationSignal"}` to demonstrate signal sending; complete the Triage task

**Given** the UC-04 `README.md.jte`
**When** the "REST API" section is read
**Then** it contains curl examples for: start; list tasks; complete Pack & Ship; `PUT /engine-rest/process-instance/{id}/suspended` with body `{"suspended": true}` to demonstrate suspension

**Given** all four curl example sets
**When** run against a locally started instance
**Then** they produce the expected responses (HTTP 200/201); variable payloads are drawn from each use case's DataInitializer values

---

## Epic 7: UI Enhancements — Navigation, Tag Colors, Footer, and Project Configuration

Improve the operaton-starter web UI with better first-time-user defaults, cleaner navigation, a build-stamped footer version, and color-coded tag chips backed by a structured tag data model. Source: PRD `docs/bmad/planning-artifacts/prds/prd-operaton-starter-2026-06-08/prd.md`.

### Story 7.1: Default Project Coordinates and Configurable Version Field

As a developer generating a new Operaton project,
I want the defaults to use a proper example groupId, a name without "example", and a version field I can set,
So that the generated project is usable as-is without immediately editing coordinates.

**Acceptance Criteria:**

**Given** the Configure page loads without `groupId` in the query string
**When** the form initializes
**Then** `form.groupId` is set to `org.operaton.example` (sourced from `metadata.defaultGroupId`)

**Given** the backend metadata endpoint
**When** `GET /api/v1/metadata` is called
**Then** the response includes `"defaultGroupId": "org.operaton.example"`

**Given** a use-case card is selected (e.g. Leave Request)
**When** the Configure page loads with `artifactId` and `projectName` from the query
**Then** neither value contains the word "example" (case-insensitive)

**Given** all project type default artifact IDs and project names in the backend metadata
**When** inspected
**Then** none contain the word "example" (case-insensitive)

**Given** the Configure page form
**When** rendered
**Then** a "Version" input field is present, labelled "Version", with default value `1.0.0-SNAPSHOT`

**Given** the Version field contains an invalid value (empty string or value containing whitespace)
**When** the user attempts to generate
**Then** an inline validation error is shown on the Version field and the Generate button is disabled

**Given** a valid version string (e.g. `2.0.0`)
**When** the user submits the form
**Then** the `version` field is included in the `POST /api/v1/generate` request body and propagated to the generated `pom.xml` or `build.gradle`

---

### Story 7.2: "Configure Now" Direct-to-Preview Flow

As a new user landing on the operaton-starter home page,
I want "Configure Now" to take me directly to the configuration form with a sensible project type pre-selected and the ability to change it,
So that I can start configuring immediately without first having to browse project types.

**Acceptance Criteria:**

**Given** a user clicks "Configure Now" on the hero section
**When** navigation occurs
**Then** the browser navigates to `/configure` with no `projectType` query parameter

**Given** `/configure` is loaded without a `projectType` query parameter (including direct URL entry and bookmarks)
**When** the form initializes
**Then** `form.projectType` is set to `PROCESS_APPLICATION` and the project type selector is rendered as an editable `<select>` (or equivalent interactive control)

**Given** the project type selector is editable
**When** the user changes the selection to a different project type
**Then** the form reflects the new project type and the file tree preview updates accordingly

**Given** `PROCESS_APPLICATION` is absent from the metadata response
**When** `/configure` loads without a `projectType` query parameter
**Then** the first project type in `metadata.projectTypes` is pre-selected

**Given** a user arrives at `/configure` via a project type card ("Configure →" button) with `?projectType=PROCESS_APPLICATION` in the URL
**When** the form renders
**Then** the project type field is read-only (existing behavior preserved)

**Given** a user arrives at `/configure` via a use-case card with `projectType` and `useCaseId` in the query
**When** the form renders
**Then** the project type field is read-only (existing behavior preserved)

---

### Story 7.3: Navigation Button Reorder and Footer Version Display

As a user on the operaton-starter home page,
I want "Project Types" to appear before "Browse Use Cases" in the hero navigation and share its style, and I want to see the running version in the footer,
So that the page hierarchy is clearer and I can identify the build at a glance.

**Acceptance Criteria:**

**Given** the GalleryView hero section
**When** rendered
**Then** the three buttons appear in this order: (1) "Configure Now →", (2) "Project Types ↓", (3) "Browse Use Cases ↓"

**Given** the "Project Types" button
**When** inspected
**Then** it uses the outlined style classes `border border-primary text-primary` (identical to the "Browse Use Cases" button)

**Given** the Vite build is run with `VITE_APP_VERSION=0.1.0-SNAPSHOT` set via Maven resource filtering
**When** the footer renders
**Then** it displays `operaton-starter 0.1.0` (suffix stripped) alongside the Apache 2.0 and operaton.org links

**Given** the Vite build is run with `VITE_APP_VERSION=1.2.3-RC1`
**When** the footer renders
**Then** it displays `operaton-starter 1.2.3` (any pre-release suffix stripped)

**Given** `VITE_APP_VERSION` is absent or empty at build time
**When** the footer renders
**Then** it displays `operaton-starter` without any version token; it does NOT display `undefined` or an empty string fragment

**Given** the Maven build is run via `mvn package` (standard, no extra flags)
**When** the resulting artifact is inspected
**Then** `VITE_APP_VERSION` was injected automatically from the POM version without manual intervention

---

### Story 7.4: Structured Tag Type and Color-Coded Chips

As a developer browsing use cases and project types,
I want tag chips to be color-coded by category,
So that I can quickly scan for projects using specific technologies, platforms, BPMN concepts, or standards.

**Acceptance Criteria:**

**Given** the OpenAPI spec and generated TypeScript types
**When** the `tags` field on `ProjectTypeInfo` and `UseCaseExample` is inspected
**Then** it is typed as `Tag[]` where `Tag = { label: string, category: TagCategory }` and `TagCategory` is an enum of `BPMN_CONCEPT | TECHNOLOGY | PLATFORM | STANDARD`

**Given** the backend metadata endpoint `GET /api/v1/metadata`
**When** called
**Then** all `tags` arrays on project types and use case examples return objects with `label` and `category` fields; no tag is a bare string

**Given** the backend metadata
**When** all tag objects are inspected
**Then** every tag has a non-null `category` value that is one of the four supported values

**Given** a tag chip with `category: BPMN_CONCEPT`
**When** rendered on any card (ProjectTypeCard, UseCaseCard, UseCaseGalleryCard)
**Then** it uses the BPMN concept color (defined by UX; meets WCAG AA contrast)

**Given** a tag chip with `category: TECHNOLOGY`
**When** rendered
**Then** it uses the Technology color (distinct from BPMN_CONCEPT, PLATFORM, STANDARD)

**Given** a tag chip with `category: PLATFORM`
**When** rendered
**Then** it uses the Platform color

**Given** a tag chip with `category: STANDARD`
**When** rendered
**Then** it uses the Standard color; the label includes the version string where applicable (e.g. "BPMN 2.0", "DMN 1.3", "CMMN 1.1")

**Given** a tag object arrives from the API with a null or unrecognized `category` value
**When** the chip renders
**Then** it uses a neutral grey fallback style and does not throw a runtime error

**Given** existing snapshot or component tests that reference tag rendering
**When** the structured tag type is adopted
**Then** all tests pass (update test fixtures to use the new `Tag` object shape)

---

## Epic 8: Examples Gallery — Remote Example Repositories

**Driver PRD:** `prds/prd-operaton-starter-examples-gallery-2026-06-13/prd.md`
**Architecture:** `architecture.md` Amendment 2026-06-13 (sections A1–A13)
**UX:** `ux-designs/ux-operaton-starter-2026-05-31/` (revised 2026-06-13)

**Goal:** Allow the operaton-starter to aggregate example projects from maintainer-configured remote GitHub repositories that publish a `.operaton-starter.yml` manifest, surface them in a new Gallery subsection alongside Project Types and Use Cases, and let users download any example as a ZIP with one click. The list of source repositories is configuration-driven and seeded with `operaton/operaton-examples`.

Story sequencing follows architecture §A12: spec freeze first, then loader, then download, then UI, then docs. Each story is independently shippable behind no flag — the feature is invisible until at least one source is configured.

### Story 8.1: Freeze Example/Tag OpenAPI Contract

As a developer building operaton-starter,
I want the `Example` model and the expanded `TagCategory` enum frozen in `openapi.yaml` before any implementation work,
So that backend, frontend, and CLI clients consume a stable contract with no per-channel divergence.

**Acceptance Criteria:**

**Given** `openapi.yaml` is updated
**When** the new `Example` schema is defined
**Then** it carries all fields from `addendum.md`'s schema table — `id`, `title`, `icon`, `path`, `shortDescription`, `longDescription`, `buildSystem`, `runtime`, `operatonVersion`, `javaVersion`, `complexity`, `tags[]`, `integrations[]`, `bpmnConcepts[]`, `requires`, `authors[]`, `license`, `documentationUrl`, `demoVideoUrl`, `screenshots[]`, `lastUpdated`, plus the computed `sourceRepo`, `sourceRepoSha`, `sourceRepoUrl`.

**Given** the `TagCategory` enum is updated
**When** the OpenAPI client is regenerated
**Then** `runtime`, `buildSystem`, and `complexity` are added as valid categories alongside the existing values; existing tag-rendering code continues to compile and render

**Given** `MetadataResponse` is extended
**When** the OpenAPI client is regenerated
**Then** a new optional `examples: Example[]` field is present; existing consumers that ignore unknown fields continue to work; no other field is removed or renamed

**Given** the generator runs in CI
**When** `openapi.yaml` is changed without regenerating clients
**Then** the existing contract-check GitHub Actions job posts a warning on the PR

### Story 8.2: Add Examples Configuration Properties and Manifest Parser

As a developer building operaton-starter,
I want `StarterProperties` extended with examples configuration and a safe YAML parser that validates manifests,
So that the loader can read a configured list of repositories and reject malformed or unsafe manifests before they enter the in-memory snapshot.

**Acceptance Criteria:**

**Given** `StarterProperties` is extended with a nested `Examples` record
**When** the application boots
**Then** `repositories: List<String>`, `cache.dir: Path` (default `${java.io.tmpdir}/operaton-starter/examples-cache`), `cache.maxSizeMb: long` (default 512), and `maxDownloadSizeMb: long` (default 50) are bound from `application.properties` and from the env vars `STARTER_EXAMPLES_REPOSITORIES`, `STARTER_EXAMPLES_CACHE_DIR`, `STARTER_EXAMPLES_CACHE_MAXSIZEMB`, `STARTER_EXAMPLES_MAXDOWNLOADSIZEMB`

**Given** the default Spring properties ship preconfigured
**When** no environment overrides are present
**Then** `starter.examples.repositories` contains the single entry `operaton/operaton-examples`

**Given** a source token is invalid (does not match `^[A-Za-z0-9._-]+/[A-Za-z0-9._-]+(@[A-Za-z0-9._/-]+)?$`)
**When** `StarterProperties` is validated at `@PostConstruct`
**Then** the invalid token is dropped, a startup warning is logged identifying the token, and the application boots normally

**Given** `ExampleManifestParser` is invoked with a well-formed manifest
**When** parsing completes
**Then** the parser returns a populated `ParsedManifest` containing each example with its computed `sourceRepo` and `sourceRepoSha`; `apiVersion` is checked against the major prefix `operaton-starter/v1`; unknown fields are silently ignored

**Given** `ExampleManifestParser` is invoked with a manifest > 256 KB, an unknown major `apiVersion`, an invalid `path` (absolute, contains `..`, or contains `\0`), or syntactically broken YAML
**When** parsing runs
**Then** the parser throws a typed `ManifestRejected` exception with a `reason` field; no partial result is returned

**Given** ArchUnit test `NoArbitraryYamlInstantiationTest` is run
**When** scanning production sources
**Then** any `Yaml` construction outside test code that does not use `SafeConstructor` fails the build

### Story 8.3: Implement GitHub Manifest Fetcher

As a developer building operaton-starter,
I want a `GitHubManifestFetcher` that resolves source tokens to manifests pinned at a commit SHA,
So that what the user sees in the gallery and what they download are guaranteed to come from the same commit.

**Acceptance Criteria:**

**Given** a source token `owner/repo` (no `@ref`)
**When** `GitHubManifestFetcher.fetch()` runs
**Then** it issues `GET https://api.github.com/repos/{owner}/{repo}/commits/HEAD` with `Accept: application/vnd.github.sha`, captures the returned 40-character SHA, then issues `GET https://raw.githubusercontent.com/{owner}/{repo}/{sha}/.operaton-starter.yml` and returns both the YAML bytes and the resolved SHA

**Given** a source token `owner/repo@some-branch`
**When** the fetch runs
**Then** the commits-API call uses `{ref}` = `some-branch`; the raw-content URL uses the resolved SHA, never the ref string

**Given** any HTTP call to GitHub exceeds 5 seconds
**When** the timeout fires
**Then** the fetcher throws `SourceUnavailable("timeout")`; no partial result is returned

**Given** the commits API returns 4xx or 5xx, or the raw URL returns non-200
**When** the fetcher runs
**Then** it throws `SourceUnavailable("http-{status}")`; the response body is not parsed

**Given** WireMock-backed tests
**When** the suite runs
**Then** there is at least one test per outcome: 200 happy path, 404 on commits, 404 on raw, 5xx, network timeout, and a manifest with a non-default branch ref

### Story 8.4: Wire Up Registry, Startup Load, and Metadata Endpoint

As a developer building operaton-starter,
I want example manifests fetched in parallel at server startup, assembled into an immutable in-memory snapshot, and exposed through the existing `/api/v1/metadata` endpoint,
So that the frontend receives examples through the same contract it already consumes.

**Acceptance Criteria:**

**Given** an `ApplicationReadyEvent` fires
**When** `ExampleRepositoryLoader.load()` runs
**Then** it dispatches one parallel task per configured source via the fetcher + parser, assembles an `ExampleSnapshot`, and stores it atomically in `ExampleRegistry`; total wall time is ≤ 3 seconds for N ≤ 10 sources on a normal network

**Given** a configured source fails (any cause)
**When** the loader completes
**Then** the source is recorded in the snapshot with an `outcome` of `skipped:<reason>` and zero examples; the application starts successfully; other sources are unaffected

**Given** every configured source fails at startup
**When** the loader completes
**Then** the application still starts; `examples[]` in `/api/v1/metadata` is an empty array; no `ErrorBanner`-equivalent server-side flag is raised

**Given** `MetadataController` serves `GET /api/v1/metadata`
**When** at least one source loaded successfully
**Then** the response includes `examples[]` with each entry carrying the manifest fields plus `sourceRepo`, `sourceRepoSha`, and `sourceRepoUrl` (HTML URL to the example folder at the pinned SHA); `useCaseExamples` and `projectTypes` are unchanged in shape

**Given** the contract test for `MetadataResponse`
**When** it runs against the live response
**Then** the response validates against the regenerated OpenAPI schema with zero violations

### Story 8.5: Implement Example Download Endpoint with SHA-Keyed ZIP Cache

As an API consumer,
I want `GET /api/v1/examples/{owner}/{repo}/{exampleId}/download` to stream a ZIP of the example subfolder pinned to the SHA the user saw,
So that the downloaded archive always matches the metadata that surfaced it, and repeat downloads are fast.

**Acceptance Criteria:**

**Given** a request to download a known example
**When** the cache holds `{cacheDir}/{owner}/{repo}/{sha}/{exampleId}.zip`
**Then** the endpoint streams the file with `Content-Type: application/zip`, `Content-Disposition: attachment; filename="{exampleId}.zip"`, an `ETag: W/"sha-{shortSha}-{exampleId}"` header, and a `Last-Modified` header from the file mtime

**Given** a request to download a known example
**When** the cache is empty for that key
**Then** `ZipBuilder` fetches `https://codeload.github.com/{owner}/{repo}/tar.gz/{sha}`, walks tar entries with Apache Commons Compress, filters to entries under the example's `path:`, re-packs them into a new `.tmp` ZIP, atomic-renames `.tmp` to the cache path, and streams the result

**Given** a tarball where the filtered uncompressed payload would exceed `maxDownloadSizeMb`
**When** the running counter trips the limit during streaming
**Then** the build aborts, the `.tmp` file is deleted, and the endpoint returns `413 Payload Too Large` with a `ProblemDetail` body identifying the example

**Given** a tar entry whose normalized path escapes the example subpath (e.g. contains `..` or is absolute)
**When** `ZipBuilder` encounters it
**Then** the build aborts, the `.tmp` file is deleted, and the endpoint returns `502 Bad Gateway` with a `ProblemDetail` describing "upstream archive failed path-safety check"

**Given** GitHub is unreachable and the cache holds no entry for the requested key
**When** the request runs
**Then** the endpoint returns `502 Bad Gateway` with a `ProblemDetail` body; the existing snapshot in `ExampleRegistry` is **not** invalidated

**Given** a request for a `(owner, repo, exampleId)` not present in the current `ExampleRegistry`
**When** the controller resolves it
**Then** the endpoint returns `404 Not Found`; no GitHub call is made

**Given** the SHA used by the download endpoint
**When** the build runs
**Then** the SHA comes from the in-memory `ExampleRegistry` snapshot — `ExampleDownloadController` never calls the commits API itself (verified by unit test that asserts no fetcher invocation on the download path)

**Given** the cache size on disk exceeds `cache.maxSizeMb`
**When** the LRU pruning task runs (every 10 minutes via `@Scheduled`)
**Then** oldest-by-last-access files are deleted until total size is below the threshold; in-flight writes (the `.tmp` files) are excluded from candidates

**Given** two concurrent requests for the same uncached `(sha, exampleId)`
**When** both reach `ZipBuilder`
**Then** both succeed (each writes its own `.tmp` then atomic-renames; the final cache entry is consistent); no partial or corrupt ZIP can be served

### Story 8.6: Implement Manual Refresh Endpoint and Diagnostics Actuator

As a maintainer,
I want a `POST /api/v1/examples/refresh` endpoint and a `/actuator/examples` diagnostics endpoint,
So that I can pick up new examples without restarting the server and can see exactly what loaded, from where, and at which SHA.

**Acceptance Criteria:**

**Given** a `POST /api/v1/examples/refresh` request
**When** the controller runs
**Then** `ExampleRepositoryLoader.load()` is re-invoked using the configured source list; the response is `200 OK` with a JSON body of per-source `Status` entries (`source`, `outcome`, `examplesLoaded`, `resolvedSha`, `lastFetchedAt`, `error?`)

**Given** a source fails during refresh while it previously had a successful snapshot in memory
**When** the new `ExampleSnapshot` is assembled
**Then** that source's slot is filled from the **previous** snapshot rather than being dropped; the response `Status` for that source carries `outcome: stale:<reason>` so the maintainer can tell it didn't update

**Given** the refresh endpoint is called repeatedly
**When** each call completes
**Then** the registry swap is atomic (an in-flight `GET /api/v1/metadata` cannot observe a torn snapshot — verified by a concurrency test)

**Given** `GET /actuator/examples` is invoked
**When** the actuator endpoint serves the response
**Then** the body matches the same `Status[]` shape returned by `/api/v1/examples/refresh`, reflecting the most recent load attempt

**Given** the refresh endpoint is unauthenticated in v1
**When** a request from any origin hits it
**Then** the controller serves it; the security review note from PRD Open Q-1 is recorded as a deferred decision (no test required, but a `@Deprecated`-style comment or `[NOTE FOR PM]` is present in the controller pointing to that decision)

### Story 8.7: Implement Examples Gallery UI (Search, Filters, Card, Download, Empty States)

As a developer evaluating Operaton,
I want a searchable, filterable Examples subsection in the gallery with rich cards and one-click ZIP download,
So that I can pick a runnable starter that fits my stack and have it on disk in under 30 seconds.

**Acceptance Criteria:**

**Given** `useExamples()`, `useGalleryFilters()`, and `useExampleDownload()` composables are implemented
**When** `GalleryView.vue` mounts
**Then** the gallery renders three subsections in this order — **Project Types**, **Examples**, **Use Cases** — each with its own `<h2>` and short blurb; the existing Marcus/Elena/Priya flows continue to enter via Project Types

**Given** `<GallerySearchBar>` is rendered
**When** the user scrolls past the hero
**Then** the search input + filter chip row sticks below the app header (`top: {spacing.header-height}`), gains `{components.search-bar.sticky-shadow}`, and stays full-width; the search input is `<input type="search" aria-label="Search examples and use cases">` with a 200ms debounce; a `<span role="status" aria-live="polite" class="sr-only">` announces "{n} examples, {m} use cases match" after each debounced change

**Given** the user toggles a filter chip
**When** rendered
**Then** the chip is `<button type="button" aria-pressed="{bool}">` with toolbar semantics (`<div role="toolbar" aria-label="Filter examples">`); Arrow Left/Right move focus between chips; Space/Enter toggles; active chips show an `×` glyph on hover/focus

**Given** the active filter state
**When** results are computed
**Then** filter chips (runtime, buildSystem, complexity, integrations) apply only to the Examples subsection; the free-text search applies to both Examples and Use Cases; filters compose with AND across categories and OR within a category; Project Types are unaffected

**Given** `<ExampleGalleryCard>` is rendered
**When** an example carries an emoji `icon`
**Then** the icon slot displays the emoji as plain text; when `icon` is a repo-relative image path, the image is fetched at the pinned SHA and rendered; when absent or load fails, a neutral default SVG glyph is shown — no error surfaced to the user

**Given** `<ExampleGalleryCard>` is rendered
**When** the user clicks "More details"
**Then** `aria-expanded` toggles on the disclosure button; the panel reveals longDescription (markdown), integrations, bpmnConcepts, requires, authors, license, lastUpdated, and the pinned short SHA in mono `0.75rem` text; `Escape` while focus is in the panel closes it and returns focus to the disclosure button

**Given** the user clicks "Download ZIP" on a card
**When** the request is in flight
**Then** the button is disabled and shows "Downloading…" with a spinner glyph; on success the button is replaced for ~3s by `{components.download-success-inline}` "Downloaded {exampleId}.zip ✓"; on failure a `{components.card-error-inline}` block appears below the action row with a "Retry" affordance; the global `<ErrorBanner>` is **not** used for per-example failures

**Given** the API returned `examples: []` (no sources loaded or all failed)
**When** the Examples subsection renders
**Then** an `<ExamplesEmptyState>` shows the copy "No examples are available right now…" with a "View format docs →" ghost button linking to `docs/examples-repository-format.md`

**Given** active filters produce no matches
**When** the Examples subsection renders
**Then** an `<ExamplesEmptyState>` shows "No examples match these filters." with a "Clear filters" ghost button that resets `useGalleryFilters().clear()`

**Given** runtime / buildSystem / complexity Tags arrive from the API
**When** rendered
**Then** they route through `tagColors.ts` to the monochrome `{components.metadata-badge}` visual lane; `concept` and `integration` Tags continue to render with the existing accent `{components.tag}` lane — verified by component tests for each category

**Given** the axe-core a11y CI job runs against `GalleryView.vue`
**When** the new components are in place
**Then** zero violations are reported; the suite covers the sticky search bar, filter chip toolbar, card disclosure pattern, and empty states

### Story 8.8: Author Manifest Format Documentation and Seed Sample Repository

As an example author,
I want clear documentation of the `.operaton-starter.yml` format and a working sample repository to copy from,
So that I can publish a new example without trial-and-error and without reading starter source code.

**Acceptance Criteria:**

**Given** `docs/examples-repository-format.md` is published
**When** read end-to-end
**Then** it covers: rationale (one paragraph), the full field-by-field schema table (matching `addendum.md`'s table), an annotated complete `.operaton-starter.yml` example, the forward-compatibility contract (unknown fields ignored, `apiVersion` major-gated), repository layout expectations (manifest at repo root; each example in a subfolder referenced by `path:`), and registration instructions (open a PR against operaton-starter to extend the default `starter.examples.repositories` list, or run the starter with `STARTER_EXAMPLES_REPOSITORIES`)

**Given** the main `README.md` is updated
**When** read
**Then** a "Contributing examples" section links to `docs/examples-repository-format.md`; the Examples Gallery section of the deployed site includes a "Publish your own examples →" link to the same doc

**Given** the seed sample manifest at `operaton/operaton-examples`
**When** committed to that repository's `main` branch
**Then** it lists at least three examples covering the runtime matrix (Spring Boot + Maven, Quarkus + Gradle, plain-Java embedded), each with a populated long description, an icon (emoji), tags spanning the new `runtime` / `buildSystem` / `complexity` categories plus existing `concept` / `integration` categories, and a `path:` pointing at a real example subfolder

**Given** the seed sample manifest validates against `ExampleManifestParser`
**When** the loader fetches it at startup
**Then** all examples in the sample load successfully (no `skipped:schema` outcomes); a smoke test runs the parser against the committed sample as a fixture

**Given** the v1 release ships
**When** an operator boots the starter with default configuration
**Then** the gallery displays the seed sample's examples without any environment override required

---

## Epic 9: Maven Archetype Integration

The `starter-archetypes` module provides `mvn archetype:generate` as a first-class generation channel, calling the shared generation engine (`starter-templates`) directly and in-process — no network dependency, no running server required. Added 2026-07-14 via sprint change proposal (`sprint-change-proposal-2026-07-14.md`), correcting the original architecture's MVP/Phase-2 split which had designated a REST-calling client as MVP.

### Story 9.1: Implement GenerationClient + EmbeddedGenerationClient

As a developer using `mvn archetype:generate`,
I want the archetype to call the generation engine directly, in-process,
So that generating a project requires no running server, no network access, and produces output identical to every other channel.

**Acceptance Criteria:**

**Given** the `GenerationClient` interface defined in `starter-archetypes`
**When** `EmbeddedGenerationClient` is invoked with a `ProjectConfig`
**Then** it calls `GenerationEngine.generate(ProjectConfig)` in `starter-templates` directly — no HTTP client, no network call, no dependency on `starter-server` being built or running

**Given** `starter-archetypes/pom.xml`
**When** dependencies are reviewed
**Then** it depends only on `starter-templates` (already the case) — no dependency on `starter-server` is introduced

**Given** the same `ProjectConfig` generated via `EmbeddedGenerationClient` and via `POST /api/v1/generate`
**When** the two outputs are compared
**Then** they are byte-for-byte identical (generation timestamps excepted)

**Given** `EmbeddedGenerationClientTest`
**When** this story is implemented
**Then** it replaces the originally-planned `RestGenerationClientTest` — no REST-calling test class exists in this module

[NOTE FOR PM/ARCHITECT]: standard Maven archetypes fill a static `archetype-resources/` tree via Velocity substitution — they don't normally invoke arbitrary Java calling `GenerationEngine.generate()`. How `mvn archetype:generate` actually triggers `EmbeddedGenerationClient` (custom Mojo? post-generate hook? something else) is NOT resolved by this story and should be pinned down before implementation starts, not during it.
