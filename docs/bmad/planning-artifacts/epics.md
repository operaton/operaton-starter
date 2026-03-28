---
stepsCompleted: ['step-01-validate-prerequisites', 'step-02-design-epics', 'step-03-create-stories', 'step-04-final-validation']
workflowStatus: complete
completedAt: '2026-03-27'
inputDocuments:
  - '_bmad-output/planning-artifacts/prd.md'
  - '_bmad-output/planning-artifacts/architecture.md'
---

# operaton-starter - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for operaton-starter, decomposing the requirements from the PRD, UX Design requirements (extracted from PRD), and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: The system can generate a project archive for any supported project type × build system combination
FR2: The system generates projects that compile and pass their included tests without manual modification
FR3: The system always generates projects targeting the current stable Operaton release (no user-selectable version)
FR4: The system propagates developer identity (Group ID, Artifact ID, project name) consistently across all generated files — Java package names, BPMN process IDs, Spring application name
FR5: The system generates a skeleton BPMN process file for applicable project types
FR6: The system generates a `processes.xml` deployment descriptor for Process Archive projects, pre-configured with the selected deployment target
FR7: The system generates deployment-target-appropriate artifact configuration (WAR/JAR) for Process Archive projects
FR8: The generation engine is a single shared implementation invoked by all channels — web UI, REST API, CLI, and MCP module; no per-channel generation logic
FR9: A developer can select a project type (MVP: Process Application, Process Archive)
FR10: A developer can select a build system (Maven, Gradle Groovy DSL, Gradle Kotlin DSL)
FR11: A developer can specify Group ID, Artifact ID, and project name as project identity
FR12: A developer can select a deployment target for Process Archive projects
FR13: A developer can choose between Dependabot and Renovate for dependency update configuration
FR14: A developer can opt in to Docker Compose file generation
FR15: A developer can opt in to GitHub Actions CI/CD skeleton generation
FR16: A developer can share a project configuration as a URL that restores and pre-fills the configuration form when opened
FR17: A developer can configure a project and download a ZIP archive through a browser
FR18: A developer can browse available project types as a visual gallery with capability descriptions
FR19: A developer can see a live file tree preview of the project to be generated, updated as configuration options change
FR20: A developer can access inline contextual help for any configuration option without leaving the page
FR21: A developer can open a generated project directly in IntelliJ IDEA or VS Code from the web UI
FR22: A developer can complete the full configuration and download flow without using a mouse
FR23: The web UI populates all configuration options and gallery content from the REST API metadata endpoint
FR24: An API consumer can generate and download a project archive via `POST /api/v1/generate` with `Accept: application/zip`
FR25: An API consumer can retrieve all supported configuration options and project template manifests via `GET /api/v1/metadata`
FR26: An API consumer can access the complete OpenAPI specification at `/api/v1/docs`
FR27: The system enforces a rate limit per IP address and returns a structured error response when exceeded
FR28: A developer can generate and download a project archive using `npx operaton-starter` with all options as command-line flags
FR29: The CLI outputs the project archive as raw bytes to stdout when stdout is a pipe, enabling shell scripting
FR30: A developer can instruct the CLI to extract the generated archive into a specified directory
FR31: An AI assistant can generate an Operaton project archive by invoking the `generate_project` MCP tool from the `operaton-starter-mcp` npm package
FR32: The `operaton-starter-mcp` package can be configured with a custom base URL to point at a self-hosted instance
FR33: Every generated project includes a README with project-specific next-step instructions tailored to the selected project type and build system
FR34: Every generated project includes a configured dependency update file (Dependabot or Renovate) ready to use without modification
FR35: Generated Process Application projects include a GitHub Actions CI/CD workflow that passes on first push
FR36: Generated projects with Docker Compose enabled include a `docker-compose.yml` that starts the application
FR37: An operator can deploy Operaton Starter as a self-hosted instance using a Docker image with no external service dependencies at startup
FR38: An operator can configure self-hosted instance defaults (default Group ID, Maven registry URL, Operaton version) via environment variables
FR39: The running instance exposes a health check endpoint for operational monitoring
FR40: A developer can access the tool through two distinct entry points: a direct configuration form and a project gallery, with both leading to the same generation flow
FR41: A developer can access an explanation distinguishing between available project types to inform their selection
FR42: The CLI and `operaton-starter-mcp` client code are generated from the OpenAPI specification; no hand-written client code exists independently of the API contract
FR43: The web UI renders the project file tree preview from template manifests in the metadata response, without a per-change server round-trip
FR44: Generated Process Application projects include a `JavaDelegate` implementation stub wired to the skeleton BPMN service task and a JUnit test that deploys and executes the skeleton process end-to-end without modification

### NonFunctional Requirements

NFR1: `POST /api/v1/generate` responds with a complete ZIP within 1 second for up to 10 concurrent requests under normal load
NFR2: Web UI live preview updates within 200ms of any configuration change
NFR3: End-to-end time from UI landing to ZIP download completes within 30 seconds
NFR4: The starter's own repository is configured with Dependabot or Renovate to automatically detect and propose Operaton version bumps
NFR5: Public instance at `start.operaton.org` achieves 99.9% uptime measured over a rolling 30-day window
NFR6: Self-hosted Docker image starts successfully with no external network calls; the generation engine has no runtime database dependency — all request state is ephemeral
NFR7: All traffic to `start.operaton.org` is served over HTTPS; HTTP requests redirect to HTTPS
NFR8: No user-identifying data is persisted; only transient IP-based data is held for rate limiting enforcement and discarded after the rate-limit window
NFR9: Rate limit enforcement returns HTTP 429 with a `Retry-After` header specifying the retry interval
NFR10: The service is horizontally scalable by adding instances; all instances are interchangeable with no sticky sessions required
NFR11: The web UI conforms to WCAG 2.1 Level AA; validated using automated accessibility tooling (e.g., axe-core) in CI and manual keyboard navigation testing before each release
NFR12: All web UI functionality is operable via keyboard navigation with visible focus indicators throughout
NFR13: Generated Process Application projects target Java 21+ and use the Spring Boot version specified in the current Operaton BOM
NFR14: Generated projects using Gradle target Gradle 8+
NFR15: The `operaton-starter-mcp` npm package supports all Node.js Active LTS versions
NFR16: Browser support for the web UI covers the latest 2 major versions of Chrome, Firefox, Safari, and Edge
NFR17: All supported project type × build system combinations (MVP: 6) are validated in CI on every template change; any failure blocks merge
NFR18: The service emits structured JSON logs compatible with standard log aggregation tools
NFR19: The Docker image is configurable entirely via environment variables; no file-based configuration is required at runtime
NFR20: The web UI visual design is consistent with the `operaton.org` and `docs.operaton.org` design system — colors, typography, and component patterns signal the same product family

### Additional Requirements

- [ARCH-1] Monorepo with Maven parent POM aggregating 5 modules: starter-server, starter-templates, starter-archetypes, starter-web, starter-mcp
- [ARCH-2] Module bootstrapping: starter-server via Spring Initializr; starter-web via `npm create vue@latest` (TypeScript, Vue Router, Vitest, ESLint+Prettier); starter-mcp via `npm init` + manual TypeScript setup; starter-templates and starter-archetypes as plain Maven modules
- [ARCH-3] JTE (Java Template Engine) as the template engine — precompiled at build time via `jte-maven-plugin`; zero runtime template parsing
- [ARCH-4] Zero Spring dependencies in starter-templates — enforced via ArchUnit test in every build
- [ARCH-5] OpenAPI spec-first discipline: `openapi.yaml` authored and frozen before any channel client code generation begins; spec freeze enforced as GitHub Actions PR status check
- [ARCH-6] Metadata schema (`/api/v1/metadata` response shape) must be defined as an architectural artifact before any channel implementation begins
- [ARCH-7] Implementation sequence: (1) OpenAPI spec + metadata schema → (2) starter-templates engine → (3) starter-server REST API + freeze spec → (4) design token extraction → (5) starter-web + starter-mcp in parallel → (6) starter-archetypes last
- [ARCH-8] Design tokens (colors, typography, spacing) extracted from `github.com/operaton/operaton.org` Jekyll source into Tailwind config before starter-web implementation
- [ARCH-9] CI/CD: GitHub Actions with `build-java`, `test-matrix` (6 parallel jobs), `contract-check`, `lint-web` on every PR/push; `docker-publish` + `npm-publish` on tagged release
- [ARCH-10] Docker base image: `eclipse-temurin:25-jre-alpine`; published to `docker.io/operaton/operaton-starter` on every tagged release
- [ARCH-11] Rate limiting: Bucket4j in-memory, best-effort per IP (10 req/min); no Redis; stateless constraint preserved
- [ARCH-12] Domain model ownership: `starter-templates` owns `ProjectConfig`, `BuildSystem` (enum), `ProjectType` (enum); no parallel definitions in other modules
- [ARCH-13] Generated project Java version picker: default Java 17; options 17, 21, 25; all three validated in CI matrix
- [ARCH-14] Pre-publish prerequisite: `org.operaton.dev` groupId must be claimed at `central.sonatype.com` before first Maven Central publish
- [ARCH-15] Structured JSON logging via Logback + `logstash-logback-encoder`; no IP address in log body
- [ARCH-16] CORS configured for `start.operaton.org` and `localhost`; self-hosted instances configure via env var
- [ARCH-17] Error responses use RFC 7807 Problem Details (`application/problem+json`) for all error responses
- [ARCH-18] frontend-maven-plugin for hermetic Node.js builds (pinned Node/npm version) in starter-web and starter-mcp Maven modules
- [ARCH-19] JTE spike required as first `starter-templates` implementation story — validate precompilation and zero-Spring constraint before full engine implementation

### UX Design Requirements

UX-DR1: The web UI landing page provides two distinct entry points: a direct configuration form (Practitioner path, for developers who know what they want) and a visual project gallery (Explorer/discovery path), both routing to the same generation flow (covers FR40)
UX-DR2: The project gallery displays visual cards for each project type with capability description, capability tags, and a persona hint (e.g. "Ideal for Camunda 7 migrators") — all content driven from the metadata endpoint (covers FR18, FR41, FR23)
UX-DR3: Every configuration option on the form has inline contextual help accessible without leaving the page — including "?" icons that expand explanatory text distinguishing project types (covers FR20, FR41)
UX-DR4: The live file tree preview renders client-side from template manifests in the metadata response and updates within 200ms of any configuration change, with no server round-trip per change (covers FR19, FR43, NFR2)
UX-DR5: The full configuration and download flow is keyboard-complete — every element focusable via Tab, all actions triggerable via keyboard, with visible focus rings throughout (covers FR22, NFR12)
UX-DR6: IDE deep-link buttons in the web UI open the generated project directly in IntelliJ IDEA or VS Code (covers FR21)
UX-DR7: The web UI generates and displays a shareable URL encoding the current configuration that restores and pre-fills the form when opened (covers FR16)
UX-DR8: The web UI visual design matches the operaton.org and docs.operaton.org design system — colors, typography, and spacing extracted as Tailwind design tokens from the operaton.org Jekyll source (covers NFR20, ARCH-8)
UX-DR9: Browser support covers the latest 2 major versions of Chrome, Firefox, Safari, and Edge (covers NFR16)
UX-DR10: The web UI meets WCAG 2.1 Level AA, validated by axe-core in CI on every PR (covers NFR11)
UX-DR11: The web UI visual design is professional, polished, and delightful — clean and purposeful without being bloated; every element earns its place; the experience should feel like a joy to use, not just functional

### FR Coverage Map

FR1: Epic 2 — Generate any project type × build system combination
FR2: Epic 2 — Generated projects compile and tests pass without modification
FR3: Epic 2 — Always targets current stable Operaton release
FR4: Epic 2 — Identity propagation (Group ID, Artifact ID, name) across all generated files
FR5: Epic 2 — Skeleton BPMN process file generation
FR6: Epic 2 — `processes.xml` deployment descriptor for Process Archive
FR7: Epic 2 — WAR/JAR artifact configuration for Process Archive
FR8: Epic 2 — Single unified generation engine across all channels
FR9: Epic 2 — Select project type
FR10: Epic 2 — Select build system (Maven, Gradle Groovy, Gradle Kotlin)
FR11: Epic 2 — Specify Group ID, Artifact ID, project name
FR12: Epic 2 — Deployment target selection for Process Archive
FR13: Epic 2 — Dependabot or Renovate choice
FR14: Epic 2 — Docker Compose opt-in
FR15: Epic 2 — GitHub Actions CI/CD skeleton opt-in
FR16: Epic 4 — Shareable configuration URL
FR17: Epic 4 — Browser-based ZIP download
FR18: Epic 4 — Visual project gallery
FR19: Epic 4 — Live file tree preview
FR20: Epic 4 — Inline contextual help
FR21: Epic 4 — IDE deep-link (IntelliJ, VS Code)
FR22: Epic 4 — Keyboard-complete flow
FR23: Epic 4 — Web UI driven from metadata endpoint
FR24: Epic 3 — `POST /api/v1/generate`
FR25: Epic 3 — `GET /api/v1/metadata`
FR26: Epic 3 — `GET /api/v1/docs` (OpenAPI spec)
FR27: Epic 3 — Rate limiting + HTTP 429 response
FR28: Epic 5 — `npx operaton-starter` with CLI flags
FR29: Epic 5 — CLI pipe mode (raw ZIP to stdout)
FR30: Epic 5 — CLI `--extract` / `--output` flags
FR31: Epic 5 — MCP `generate_project` tool
FR32: Epic 5 — MCP configurable base URL
FR33: Epic 2 — Project-specific README with next steps
FR34: Epic 2 — Dependabot/Renovate config in generated project
FR35: Epic 2 — GitHub Actions CI/CD skeleton in generated project
FR36: Epic 2 — `docker-compose.yml` in generated project
FR37: Epic 6 — Self-hosted Docker image
FR38: Epic 6 — Env-var defaults for self-hosted instance
FR39: Epic 3 — `/actuator/health` health endpoint
FR40: Epic 4 — Two entry points: form + gallery
FR41: Epic 4 — Project type disambiguation explanation
FR42: Epic 5 — CLI + MCP clients generated from OpenAPI spec
FR43: Epic 4 — Client-side preview from template manifests
FR44: Epic 2 — JavaDelegate stub + end-to-end JUnit test

## Epic List

### Epic 1: Monorepo Foundation & API Contract
The monorepo is bootstrapped, the OpenAPI spec and metadata schema are defined as the definitive contract, all modules are set up so every contributor can build from source, and the repo is wired for automated Operaton version tracking from day one.
**FRs covered:** FR25 (metadata schema defined), FR26 (OpenAPI spec authored)
**ARCH covered:** ARCH-1, ARCH-2, ARCH-5, ARCH-6, ARCH-7, ARCH-9, ARCH-14, ARCH-18
**NFRs:** NFR4

### Epic 2: Core Generation Engine
Any developer can invoke the pure-Java generation engine and receive a valid, compiling, immediately runnable Operaton project archive for any project type × build system combination — with correct identity propagation, skeleton BPMN, all generated project extras, and a full CI validation matrix.
**FRs covered:** FR1–15, FR33–36, FR44
**ARCH covered:** ARCH-3, ARCH-4, ARCH-12, ARCH-13, ARCH-19
**NFRs:** NFR1 (partial), NFR13, NFR14, NFR17

### Epic 3: REST API — Programmatic Project Generation
Any developer with internet access (or a curl command) can generate an Operaton project via `POST /api/v1/generate`, inspect all configuration options via the metadata endpoint, and read the OpenAPI spec — making the service usable from any tool, script, or integration.
**FRs covered:** FR24–27, FR39, FR42 (partial — OpenAPI generator toolchain wired)
**ARCH covered:** ARCH-11, ARCH-15, ARCH-16, ARCH-17
**NFRs:** NFR1, NFR5, NFR7, NFR8, NFR9, NFR10, NFR18

### Epic 4: Web UI — Browser-Based Project Generation
Practitioners complete configuration and download a ZIP in under 30 seconds. Explorers discover their project type through a visual gallery. Both enjoy a professional, keyboard-accessible, operaton.org-consistent interface with live preview, IDE deep-links, and shareable config URLs — benchmarked against start.spring.io and code.quarkus.io.
**FRs covered:** FR16–23, FR40–41, FR43
**UX-DRs covered:** UX-DR1 through UX-DR11
**ARCH covered:** ARCH-8
**NFRs:** NFR2, NFR3, NFR11, NFR12, NFR16, NFR20

### Epic 5: CLI & MCP — Terminal and AI-Native Access
Developers generate projects from `npx operaton-starter` in scriptable or interactive mode. AI assistants (Claude, GitHub Copilot, Cursor) generate projects mid-conversation via the `operaton-starter-mcp` npm tool. All four access channels are live.
**FRs covered:** FR28–32, FR42
**NFRs:** NFR15

---

## Epic 1: Monorepo Foundation & API Contract

The monorepo is bootstrapped, the OpenAPI spec and metadata schema are defined as the definitive contract, all modules are set up so every contributor can build from source, and the repo is wired for automated Operaton version tracking from day one.

### Story 1.1: Bootstrap Monorepo Structure

As a **developer contributing to operaton-starter**,
I want the monorepo to be fully bootstrapped with all modules wired and building green,
So that I can clone the repository and start working immediately without any manual setup.

**Acceptance Criteria:**

**Given** a developer clones the repository
**When** they run `mvn verify` from the project root
**Then** all 5 Maven modules build successfully with zero compilation errors and zero test failures

**Given** the monorepo structure
**When** inspecting the root `pom.xml`
**Then** it declares all 5 modules as children: `starter-server`, `starter-templates`, `starter-archetypes`, `starter-web`, `starter-mcp`

**Given** `starter-server`
**When** inspected
**Then** it is bootstrapped from Spring Initializr with Spring Boot 4.0.4, Java 21, Maven, and dependencies: `spring-boot-starter-web`, `spring-boot-starter-actuator`, `spring-boot-starter-validation`; the Operaton BOM 2.0.0 is added as an imported BOM; root package is `org.operaton.dev.starter.server`

**Given** `starter-templates`
**When** inspected
**Then** it is a plain Maven module with root package `org.operaton.dev.starter.templates`, zero Spring dependencies in its POM, and an ArchUnit test that fails the build if any class imports from `org.springframework.*`

**Given** a class importing from `org.springframework.*` is introduced into `starter-templates`
**When** `mvn verify` is run
**Then** the ArchUnit test fails the build with a clear violation message

**Given** `starter-archetypes`
**When** inspected
**Then** it is a plain Maven module with root package `org.operaton.dev.starter.archetypes`

**Given** `starter-web`
**When** inspected
**Then** it is scaffolded via `npm create vue@latest` with TypeScript, Vue Router, Vitest, ESLint+Prettier (no Pinia); its Maven POM uses `frontend-maven-plugin` with pinned Node.js v22 and npm 10 to run `npm ci && npm run build` during `mvn verify`

**Given** `starter-mcp`
**When** inspected
**Then** it is scaffolded via `npm init` with TypeScript and `@modelcontextprotocol/sdk@1.28.0`; its Maven POM uses `frontend-maven-plugin` identically to `starter-web`

**Given** the project root
**When** inspected
**Then** it contains: `renovate.json` (file present); `.editorconfig`; `.gitignore`; `README.md` (skeleton); `docker-compose.dev.yml` (skeleton)

> **Note:** `org.operaton.dev` groupId must be claimed at `central.sonatype.com` before the first Maven Central release is attempted.

### Story 1.2: Author OpenAPI Spec & Metadata Schema

As a **developer building any channel** (web UI, CLI, MCP, or integration),
I want a complete OpenAPI spec and metadata schema to exist as the single source of truth,
So that I can generate client code and implement against a stable contract without coordination overhead.

**Acceptance Criteria:**

**Given** the project root
**When** inspected
**Then** `openapi.yaml` exists and is a valid OpenAPI 3.x document covering the two developer-facing API endpoints: `POST /api/v1/generate` and `GET /api/v1/metadata`

**Given** `POST /api/v1/generate` in `openapi.yaml`
**When** inspected
**Then** the request body schema defines all project configuration fields: `groupId`, `artifactId`, `projectName`, `projectType`, `buildSystem`, `javaVersion`, `deploymentTarget` (optional — validated server-side for Process Archive), `dependencyUpdater`, `dockerCompose` (boolean), `githubActions` (boolean); all fields use `camelCase`; required fields are declared

**Given** `GET /api/v1/metadata` in `openapi.yaml`
**When** inspected
**Then** the response schema defines the full metadata shape: `projectTypes[]` (each with `id`, `displayName`, `description`, `tags`, `personaHint`, `templateManifest[]`), `buildSystems[]` (each with `id`, `displayName`), and `globalOptions.javaVersions` (options: [17, 21, 25], default: 17)

**Given** `templateManifest` entries in the metadata schema
**When** inspected
**Then** each entry defines: `path` (string), `condition` (string or null), `templateId` (string)

**Given** `openapi.yaml`
**When** validated with the OpenAPI Generator tool
**Then** it produces valid server stubs and client code with zero errors

**Given** `starter-server`
**When** inspected
**Then** `openapi.yaml` is referenced by `openapi-generator-maven-plugin` in the POM and server stubs are generated into `src/generated/` at build time; no hand-written DTOs duplicate the generated types

**Given** the `/api/v1/docs` path
**When** accessed in a browser
**Then** it serves a static HTML page that renders the OpenAPI spec via Scalar loaded from CDN; this path is not declared as an endpoint in `openapi.yaml` itself — `openapi.yaml` is served as a static resource from `src/main/resources/static/`

**Given** the `src/generated/` directory in any module
**When** inspected
**Then** it contains only OpenAPI-generated files; no manually authored files exist within it

> **Note:** Spec freeze enforcement (GitHub Actions `contract-check` job) is wired in Story 1.3. Until then, spec discipline is by convention.

### Story 1.3: GitHub Actions CI Pipeline

As a **developer contributing to operaton-starter**,
I want every pull request to be validated by an automated CI pipeline,
So that broken builds, spec drift, and lint failures are caught before they reach the main branch.

**Acceptance Criteria:**

**Given** a pull request is opened or pushed to
**When** GitHub Actions triggers
**Then** four jobs run in parallel: `build-java`, `test-matrix`, `contract-check`, and `lint-web`

**Given** the `build-java` job
**When** it runs
**Then** it executes `mvn verify` on the full monorepo and fails the PR if any module fails to compile or any test fails

**Given** the `test-matrix` job
**When** inspected
**Then** it is structured as a GitHub Actions matrix job with placeholder dimensions, ready to accept project type × build system combinations in Epic 2 without modifying the CI file structure

**Given** the `contract-check` job
**When** it runs
**Then** it validates that all generated client stubs are consistent with the current `openapi.yaml`; it posts a **warning-level** status check to the PR (not a hard merge block at this stage — promotes to hard block in Phase 2 once the spec is stable)

**Given** the `lint-web` job
**When** it runs
**Then** it executes ESLint and Vitest for both `starter-web` and `starter-mcp`; any lint error or Vitest test failure fails the PR; a zero-test-suite result in `starter-mcp` is not treated as a failure

**Given** a tagged release (e.g. `v1.0.0`)
**When** the release workflow triggers
**Then** `docker-publish` and `npm-publish` stub jobs run, wired to tag triggers; the publish steps are stubs to be completed in Epics 5 and 6 respectively — no Docker image or npm package is published on non-tagged commits

**Given** the CI pipeline with the monorepo in its bootstrapped state (Stories 1.1 and 1.2 complete)
**When** triggered on first push
**Then** all four CI jobs pass green with no manual intervention

---

## Epic 2: Core Generation Engine

Any developer can invoke the pure-Java generation engine and receive a valid, compiling, immediately runnable Operaton project archive for any project type × build system combination — with correct identity propagation, skeleton BPMN, all generated project extras, and a full CI validation matrix.

### Story 2.1: JTE Spike — Validate Template Engine & Zero-Spring Constraint

As a **developer implementing the generation engine**,
I want to validate that JTE templates precompile correctly at build time with zero Spring dependencies,
So that the team has confirmed the performance and constraint foundations before investing in full engine implementation.

**Acceptance Criteria:**

**Given** `starter-templates`
**When** inspected
**Then** `gg.jte:jte` and `gg.jte:jte-maven-plugin` are declared as dependencies; the JTE plugin version is declared in the parent POM `<pluginManagement>` section; the `jte-maven-plugin` is configured to precompile all templates at build time into generated Java classes

**Given** a minimal sample JTE template exists in `starter-templates` (e.g. a trivial `pom.xml.jte` stub)
**When** `mvn verify` runs
**Then** the plugin compiles the template to a Java class at build time; no JTE template files are parsed at runtime; the compiled template classes are present in the output JAR

**Given** a unit test in `starter-templates` that invokes the compiled template
**When** the test runs
**Then** it generates output from the precompiled template in-memory and asserts the output contains expected content — no Spring context, no file I/O, no subprocess invocation required

**Given** the ArchUnit zero-Spring test from Story 1.1
**When** `mvn verify` runs after adding JTE
**Then** the ArchUnit test still passes — JTE introduces no Spring dependency into `starter-templates`

**Given** the compiled template is invoked 100 times in a loop in a unit test
**When** the test runs
**Then** total execution completes in under 500ms — demonstrating precompilation eliminates runtime parsing overhead

**Given** the spike is validated
**When** reviewed
**Then** the sample template is moved to `starter-templates/spike/` with a brief README — it is not left in the engine source path; an implementation note committed to `docs/arc42/04-solution-strategy.md` confirms: (1) JTE precompilation works with the `jte-maven-plugin` version used, (2) the zero-Spring constraint holds with JTE on the classpath, (3) 100 template invocations complete under 500ms

### Story 2.2: Domain Model & Project Configuration

As a **developer implementing the generation engine**,
I want the shared domain model and `GenerationEngine` public API to be defined in `starter-templates`,
So that all modules have a single source of truth for project configuration types and the engine has a clear, testable entry point.

**Acceptance Criteria:**

**Given** `starter-templates`
**When** inspected
**Then** the following types exist in `org.operaton.dev.starter.templates.model`: `ProjectConfig` (Java record), `ProjectType` (enum: `PROCESS_APPLICATION`, `PROCESS_ARCHIVE`), `BuildSystem` (enum: `MAVEN`, `GRADLE_GROOVY`, `GRADLE_KOTLIN`), `DeploymentTarget` (enum: `TOMCAT`, `STANDALONE_ENGINE`)

**Given** `ProjectConfig`
**When** inspected
**Then** it is a Java record holding all project configuration fields: `groupId`, `artifactId`, `projectName`, `projectType`, `buildSystem`, `javaVersion` (int, default 17), `deploymentTarget` (Optional\<DeploymentTarget\>), `dependencyUpdater` (enum: `DEPENDABOT`, `RENOVATE`), `dockerCompose` (boolean), `githubActions` (boolean)

**Given** `starter-templates`
**When** inspected
**Then** a `GenerationEngine` class exists in `org.operaton.dev.starter.templates` with a single public method: `byte[] generate(ProjectConfig config)`

**Given** a `ProjectConfig` with any valid combination of fields
**When** `GenerationEngine.generate(config)` is called
**Then** it returns a non-empty `byte[]` whose contents are readable as a valid ZIP by `java.util.zip.ZipInputStream` without exception; the ZIP contains at least one file (e.g. a stub `pom.xml`)

**Given** `ProjectType`, `BuildSystem`, `DeploymentTarget`, and `ProjectConfig` are defined in `starter-templates`
**When** any other module needs these types
**Then** it imports them from `starter-templates` — no module defines its own parallel representation

**Given** the ArchUnit zero-Spring test
**When** `mvn verify` runs after adding the domain model
**Then** it still passes — no Spring imports introduced

**Given** a `@ParameterizedTest` covering all 6 valid `ProjectType` × `BuildSystem` combinations
**When** `GenerationEngine.generate(config)` is called for each combination
**Then** it completes without exception, returns a non-null non-empty `byte[]`, and the result is a valid ZIP readable by `ZipInputStream` without exception

### Story 2.3: Process Application Generation (Maven)

As a **Java developer starting a new Operaton project**,
I want to generate a fully working Process Application project with Maven build,
So that I can clone it, run `mvn spring-boot:run`, and have a running Operaton engine with a deployed skeleton process in under 3 minutes.

**Acceptance Criteria:**

**Given** a `ProjectConfig` with `projectType=PROCESS_APPLICATION`, `buildSystem=MAVEN`, and valid `groupId`, `artifactId`, `projectName`, `javaVersion`
**When** `GenerationEngine.generate(config)` is called
**Then** the returned ZIP contains: `pom.xml`, `src/main/java/{package}/Application.java`, `src/main/java/{package}/delegate/SkeletonDelegate.java`, `src/main/resources/{artifactId}.bpmn`, `src/main/resources/application.properties`, `src/test/java/{package}/ProcessIT.java`

**Given** the generated `pom.xml`
**When** inspected
**Then** it declares: `groupId` matching the configured value, `artifactId` matching the configured value, Spring Boot parent POM at the version from the current Operaton BOM, Operaton BOM imported, Java version matching `javaVersion` config; no hardcoded Operaton version — driven from BOM

**Given** the generated Java source files
**When** inspected
**Then** all Java package declarations match `{groupId}.{artifactId}` with dots replacing hyphens; no file contains a hardcoded package path

**Given** the generated `{artifactId}.bpmn`
**When** inspected
**Then** the BPMN process ID contains `artifactId`; it includes one service task wired to `SkeletonDelegate` by class reference

**Given** the generated `SkeletonDelegate.java`
**When** inspected
**Then** it implements `org.operaton.bpm.engine.delegate.JavaDelegate` and is wired to the skeleton BPMN service task

**Given** the generated `application.properties`
**When** inspected
**Then** `spring.application.name` matches `projectName`

**Given** the generated `ProcessIT.java`
**When** the test is run in a project compiled from the generated ZIP
**Then** it deploys the skeleton BPMN process and executes it end-to-end without manual modification — the test passes on first run

**Given** a `@ParameterizedTest` in `starter-templates` testing the Maven Process Application combination
**When** it runs
**Then** it calls `GenerationEngine.generate(config)`, extracts the ZIP in-memory, and asserts: all expected files exist at correct paths, identity fields propagate correctly into `pom.xml`, package names, BPMN process ID, and `application.name`

### Story 2.4: Process Application Generation (Gradle Groovy & Kotlin DSL)

As a **Java developer who prefers Gradle**,
I want to generate a fully working Process Application project with Gradle Groovy DSL or Gradle Kotlin DSL,
So that I get the same ready-to-run experience as Maven users — just with my preferred build tool.

**Acceptance Criteria:**

**Given** a `ProjectConfig` with `projectType=PROCESS_APPLICATION`, `buildSystem=GRADLE_GROOVY`
**When** `GenerationEngine.generate(config)` is called
**Then** the ZIP contains `build.gradle` (Groovy DSL) instead of `pom.xml`; `settings.gradle` with `rootProject.name` matching `projectName`; all Java sources, BPMN, and test files identical in structure to Story 2.3 including `ProcessIT.java`

**Given** a `ProjectConfig` with `projectType=PROCESS_APPLICATION`, `buildSystem=GRADLE_KOTLIN`
**When** `GenerationEngine.generate(config)` is called
**Then** the ZIP contains `build.gradle.kts` (Kotlin DSL) instead of `pom.xml`; `settings.gradle.kts` with `rootProject.name` matching `projectName`; all Java sources, BPMN, and test files identical in structure to Story 2.3 including `ProcessIT.java`

**Given** both Gradle-generated `build.gradle` and `build.gradle.kts`
**When** inspected
**Then** they declare: Operaton BOM imported, Spring Boot plugin at the BOM-specified version, Java version matching `javaVersion` config; Gradle wrapper files (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) are included; `gradle-wrapper.properties` targets a specific Gradle 8.x version pinned in the generation template

**Given** identity propagation
**When** either Gradle build file is inspected
**Then** `group` matches `groupId`, `version` is set to `0.0.1-SNAPSHOT`, and the project name in `settings.gradle(.kts)` matches `projectName`

**Given** the single `@ParameterizedTest` in `starter-templates` with a static combination provider (introduced in Story 2.3)
**When** Story 2.4 is complete
**Then** the provider includes rows for `GRADLE_GROOVY` and `GRADLE_KOTLIN` Process Application combinations; each row asserts: correct build file type present, no `pom.xml` present, identity fields propagate correctly, BPMN process ID contains `artifactId`, `ProcessIT.java` is present

### Story 2.5: Process Archive Generation

As a **Java developer deploying to a shared Operaton engine**,
I want to generate a Process Archive project for my chosen deployment target,
So that I receive a deployable WAR or JAR with a correctly configured `processes.xml` that works on first deployment.

**Acceptance Criteria:**

**Given** a `ProjectConfig` with `projectType=PROCESS_ARCHIVE`, `buildSystem=MAVEN`, `deploymentTarget=TOMCAT`
**When** `GenerationEngine.generate(config)` is called
**Then** the ZIP contains: `pom.xml` configured for WAR packaging, `src/main/resources/META-INF/processes.xml` pre-configured with the archive name and `deploymentTarget=TOMCAT`, `src/main/resources/{artifactId}.bpmn` (skeleton BPMN), no `Application.java` (no embedded engine)

**Given** a `ProjectConfig` with `projectType=PROCESS_ARCHIVE`, `deploymentTarget=STANDALONE_ENGINE`
**When** `GenerationEngine.generate(config)` is called
**Then** the ZIP contains `processes.xml` pre-configured with `deploymentTarget=STANDALONE_ENGINE`; packaging is JAR

**Given** the generated `processes.xml`
**When** inspected
**Then** the process archive name matches `artifactId`; the engine reference reflects the selected `deploymentTarget`; no hardcoded values exist — all driven from `ProjectConfig`

**Given** Process Archive projects
**When** inspected
**Then** no `SkeletonDelegate.java`, no `ProcessIT.java`, and no Spring Boot application class are generated — Process Archive is engine-agnostic and has no embedded engine

**Given** the single `@ParameterizedTest` combination provider
**When** Story 2.5 is complete
**Then** it includes rows for `PROCESS_ARCHIVE` × all three build systems × both deployment targets; each row asserts: correct packaging type, `processes.xml` present with correct content, no embedded engine classes present, identity propagation correct

**Given** all 6 MVP project type × build system combinations (2 types × 3 build systems)
**When** the parameterized test suite runs after Story 2.5
**Then** all 6 combinations pass — this completes the core generation matrix

### Story 2.6: Generated Project Extras (README, CI, Docker Compose, Dependabot/Renovate)

As a **developer who just generated an Operaton project**,
I want the generated ZIP to include a tailored README, dependency update config, and optional CI/Docker Compose files,
So that my project is production-ready from the first commit — no boilerplate hunting required.

**Acceptance Criteria:**

**Given** any generated project
**When** the ZIP is inspected
**Then** it contains a `README.md` tailored to the selected `projectType` and `buildSystem` — including the correct run command (`mvn spring-boot:run` or `./gradlew bootRun`), a "What to do next" section with generic contextual doc link placeholders (not hardcoded URLs), and a "Troubleshooting" section naming the three most common startup failure modes: (1) port 8080 already in use, (2) H2 in-memory datasource not configured, (3) Java version mismatch between runtime and compiled bytecode

**Given** `dependencyUpdater=DEPENDABOT`
**When** the ZIP is inspected
**Then** it contains `.github/dependabot.yml` configured for the project's build system; no `renovate.json` is present

**Given** `dependencyUpdater=RENOVATE`
**When** the ZIP is inspected
**Then** it contains `renovate.json` configured for the project's build system; no `.github/dependabot.yml` is present

**Given** `githubActions=true` and `projectType=PROCESS_APPLICATION`
**When** the ZIP is inspected
**Then** it contains `.github/workflows/ci.yml` that compiles the project and runs `ProcessIT.java`; the workflow targets the Java version matching `javaVersion` config

**Given** `githubActions=true` and `projectType=PROCESS_ARCHIVE`
**When** the ZIP is inspected
**Then** it contains `.github/workflows/ci.yml` that compiles the project and runs whatever tests exist; no assumption is made about specific test class names

**Given** `githubActions=false`
**When** the ZIP is inspected
**Then** no `.github/workflows/` directory is present

**Given** `dockerCompose=true`
**When** the ZIP is inspected
**Then** it contains `docker-compose.yml` that starts the application with correct image and port bindings for the selected `projectType`

**Given** `dockerCompose=false`
**When** the ZIP is inspected
**Then** no `docker-compose.yml` is present

**Given** the `@ParameterizedTest` combination provider
**When** Story 2.6 is complete
**Then** test rows cover both `dependencyUpdater` values, both `githubActions` values, and both `dockerCompose` values — asserting correct file presence and absence for each combination

### Story 2.7: CI Test Matrix — Validate All 6 Combinations

As a **developer merging a template change**,
I want the CI pipeline to compile and test all 6 project type × build system combinations on every PR,
So that no template change can silently break any supported combination.

> **Sequencing note:** This story is implemented after Stories 2.3–2.6 are complete — the matrix requires all 6 combinations to be implemented before it can validate them.

**Acceptance Criteria:**

**Given** the `test-matrix` GitHub Actions job (stub created in Story 1.3)
**When** Story 2.7 is complete
**Then** it is a full matrix job with dimensions `projectType: [PROCESS_APPLICATION, PROCESS_ARCHIVE]` × `buildSystem: [MAVEN, GRADLE_GROOVY, GRADLE_KOTLIN]` — 6 parallel jobs total

**Given** each matrix job
**When** it runs
**Then** it executes as a CI shell step: (1) invokes `GenerationEngine.generate(config)` for its combination via `mvn exec:java` or equivalent, (2) extracts the ZIP to a temp directory, (3) `cd`s into that directory, (4) runs the appropriate build command — `mvn verify` for Maven, `./gradlew build` for Gradle; the job fails if compilation fails or any test fails

**Given** each matrix job
**When** inspected
**Then** it uses the same pinned Java version as the rest of the CI pipeline — no version drift between build environment and what is being validated

**Given** a template change that breaks one combination
**When** the matrix runs
**Then** only the affected job fails — other combinations continue and report independently; the PR is blocked from merging; this is a hard merge block (unlike `contract-check` which is warning-level)

**Given** all 6 matrix jobs pass
**When** a PR is reviewed
**Then** the GitHub Actions status panel shows 6 green matrix job entries — one per combination — before merge is permitted

**Given** the `@ParameterizedTest` suite in `starter-templates` (built across Stories 2.3–2.6)
**When** `mvn verify` runs
**Then** all combination rows pass — the unit-level parameterized tests validate ZIP contents in-memory; the CI matrix jobs validate that extracted projects actually build; these are complementary layers, not duplicates

**Given** the CI matrix includes one additional smoke-test job for the Maven Process Application combination
**When** the job runs
**Then** it extracts the generated ZIP, runs `mvn spring-boot:run` in the background, polls `GET http://localhost:8080/actuator/health` until it returns `200 OK` (timeout: 60 seconds), then stops the process; the job fails if the application does not start within 60 seconds — this validates the PRD's hard guarantee that every generated Process Application starts without manual modification

---

## Epic 3: REST API — Programmatic Project Generation

Any developer with internet access can generate an Operaton project via `POST /api/v1/generate`, inspect all configuration options via the metadata endpoint, and read the OpenAPI spec — making the service usable from any tool, script, or integration.

### Story 3.1: Wire Generation Engine into REST API (`POST /api/v1/generate`)

As a **developer or tool author**,
I want to generate an Operaton project archive by posting a configuration JSON to the REST API,
So that I can automate project generation from any HTTP client, script, or integration without using a browser.

**Acceptance Criteria:**

**Given** a valid `POST /api/v1/generate` request with `Accept: application/zip` and a correct configuration JSON body
**When** the endpoint is called
**Then** it returns `200 OK` with `Content-Type: application/zip`, `Content-Disposition: attachment; filename="{artifactId}.zip"`, and a ZIP body produced by `GenerationEngine.generate(config)`

**Given** the `starter-server` controller
**When** inspected
**Then** it uses the OpenAPI-generated server stub (from Story 1.2) as its interface — no hand-written request/response DTOs exist; a `ProjectConfigMapper` class in `starter-server` translates the generated DTO to `ProjectConfig`; no mapping logic exists in the controller itself

**Given** a request body with a missing required field (e.g. no `groupId`)
**When** `POST /api/v1/generate` is called
**Then** it returns `400 Bad Request` with `Content-Type: application/problem+json` and an RFC 7807 Problem Details body naming the invalid field; no stack trace is included in the response

**Given** a request with `projectType=PROCESS_ARCHIVE` and no `deploymentTarget`
**When** `POST /api/v1/generate` is called
**Then** it returns `400 Bad Request` with a Problem Details body explaining that `deploymentTarget` is required for Process Archive projects

**Given** a request body containing an unknown `projectType` enum value
**When** `POST /api/v1/generate` is called
**Then** it returns `400 Bad Request` with a Problem Details body — not `500 Internal Server Error`; the deserialization error is caught by the `@ControllerAdvice`

**Given** a single `@ControllerAdvice` in `starter-server`
**When** any domain or validation exception is thrown
**Then** it translates all exceptions to RFC 7807 Problem Details responses; no try/catch exists in the controller itself; the `@ControllerAdvice` logs at `ERROR` level with full stack trace while the client receives no stack trace

**Given** a successful generation request
**When** the request completes
**Then** `starter-server` emits a structured JSON log entry at `INFO` level containing: `projectType`, `buildSystem`, `javaVersion`; no IP address is included in the log body

**Given** a `@Test` that calls `POST /api/v1/generate` 10 times concurrently
**When** the test runs
**Then** the median response time is under 1 second; no request fails or times out

**Given** `starter-server`
**When** inspected
**Then** CORS is configured to allow browser requests from `start.operaton.org` and `localhost`; self-hosted instances can override allowed origins via environment variable

**Given** the `docker-compose.dev.yml` at the project root
**When** expanded in this story to include the backend service
**Then** a `starter-web` developer can run `docker compose -f docker-compose.dev.yml up` to start the backend API at `http://localhost:8080`; the Vite dev server in `starter-web` proxies API calls to this backend — no Epic 6 work is required for local frontend development to function

### Story 3.2: Metadata Endpoint (`GET /api/v1/metadata`)

As a **developer or tool author consuming the API**,
I want to retrieve all supported configuration options and template manifests from a single endpoint,
So that I can build clients and previews driven entirely by the API without hardcoding any option lists.

**Acceptance Criteria:**

**Given** a `GET /api/v1/metadata` request
**When** the endpoint is called
**Then** it returns `200 OK` with `Content-Type: application/json` and a response body matching the metadata schema defined in `openapi.yaml` (Story 1.2): `projectTypes[]`, `buildSystems[]`, `globalOptions.javaVersions`

**Given** the `projectTypes` array in the response
**When** inspected
**Then** it contains entries for `PROCESS_APPLICATION` and `PROCESS_ARCHIVE`; each entry includes `id`, `displayName`, `description`, `tags`, `personaHint`, and `templateManifest[]`

**Given** each `templateManifest` entry
**When** inspected
**Then** it contains `path`, `condition` (string or null), and `templateId`; the manifest accurately reflects the files generated for that project type by the engine — no hardcoded lists exist in any channel

**Given** the `globalOptions.javaVersions` field
**When** inspected
**Then** it returns `{ "options": [17, 21, 25], "default": 17 }`

**Given** no hardcoded option lists exist in `starter-web`, CLI, or MCP
**When** those channels need configuration options
**Then** they fetch from `GET /api/v1/metadata` — the metadata endpoint is the single source of truth for all channels

**Given** the metadata response
**When** measured
**Then** `GET /api/v1/metadata` responds within 200ms under normal load — it serves a static in-memory data structure, not a database query

### Story 3.3: Rate Limiting, Error Handling & Health Endpoint

As a **service operator**,
I want the API to enforce rate limits per IP, expose a health endpoint, and return consistent structured errors,
So that the public instance is protected from abuse and operational monitoring works out of the box.

**Acceptance Criteria:**

**Given** a client sending more than 10 requests per minute from the same IP
**When** the 11th request arrives within the rate-limit window
**Then** the API returns `429 Too Many Requests` with a `Retry-After` header specifying the retry interval in seconds and a Problem Details body with `type: "https://start.operaton.org/errors/rate-limit-exceeded"`

**Given** the rate limiting implementation
**When** inspected
**Then** it uses Bucket4j in-memory per-IP token buckets; the rate limit window and request cap are configurable via properties (e.g. `rate-limit.requests-per-minute=10`) enabling tests to use a short window (e.g. 1 second, 2 requests) to validate 429 responses without slow tests

**Given** the service is deployed behind a reverse proxy
**When** a request arrives with an `X-Forwarded-For` header
**Then** rate limiting keys on the real client IP extracted from `X-Forwarded-For`; falls back to the direct connection IP if the header is absent; the proxy IP is never used as the bucket key

**Given** a rate-limited request
**When** the server logs are inspected
**Then** a `WARN` level structured JSON log entry is emitted; the log entry does not include the client IP address in the log body

**Given** `GET /actuator/health`
**When** called and the service is healthy
**Then** it returns `200 OK` with `{ "status": "UP" }`

**Given** `GET /actuator/health`
**When** called and the service is unhealthy
**Then** it returns `503 Service Unavailable` — compatible with load balancer health checks; only the health endpoint is exposed via Actuator by default

**Given** any unhandled server-side exception
**When** it reaches the `@ControllerAdvice`
**Then** it returns `500 Internal Server Error` with a Problem Details body; no stack trace or internal detail is included in the response body; the full exception is logged at `ERROR` level server-side

**Given** the service deployed horizontally across multiple instances
**When** requests are distributed across instances
**Then** each instance enforces rate limiting independently from its own in-memory bucket — no cross-instance coordination required; explicitly accepted as best-effort behaviour

### Story 3.4: OpenAPI Docs Endpoint & Spec Freeze Enforcement

As a **developer integrating with the REST API**,
I want to browse interactive API documentation and have CI enforce that client code never drifts from the spec,
So that the API contract is always trustworthy and integration is friction-free.

**Acceptance Criteria:**

**Given** `GET /api/v1/docs` in a browser
**When** the page loads
**Then** it renders the Scalar API reference UI loaded from CDN, pointing at the static `openapi.yaml` served from `/api/v1/openapi.yaml`; the page is functional with no Spring Boot version coupling; the Scalar page applies operaton.org brand colours where Scalar's theming API permits

**Given** `GET /api/v1/openapi.yaml`
**When** called
**Then** it returns the current `openapi.yaml` as a static resource with `Content-Type: application/yaml`

**Given** `openapi.yaml` is modified in a PR
**When** the `contract-check` CI job runs
**Then** it regenerates all client stubs from the updated `openapi.yaml` and diffs them against the committed `src/generated/` files in all modules; any diff fails the job and posts a **hard block** merge status (promoted from warning-level in Story 1.3 — spec is now stable)

**Given** any file in `src/generated/` in any module
**When** a PR attempts to modify it manually
**Then** the `contract-check` job detects the diff and fails — generated files must only be updated by re-running the OpenAPI generator, never hand-edited

**Given** the Scalar docs page
**When** a developer uses the interactive try-it feature to send `POST /api/v1/generate`
**Then** the request reaches the live API and returns a valid ZIP — works against both the production instance (`start.operaton.org`) and local dev (`localhost`) via the CORS configuration from Story 3.1

---

Does this look right? Any adjustments before saving and completing Epic 3?

---

## Epic 4: Web UI — Browser-Based Project Generation

Practitioners complete configuration and download a ZIP in under 30 seconds. Explorers discover their project type through a visual gallery. Both enjoy a professional, keyboard-accessible, operaton.org-consistent interface with live preview, IDE deep-links, and shareable config URLs — benchmarked against start.spring.io and code.quarkus.io.

### Story 4.1: Design Token Extraction & Vue App Foundation

As a **developer visiting `start.operaton.org`**,
I want the web app to load with operaton.org's visual identity — colours, typography, and spacing — and have working navigation between the gallery and configuration views,
So that the tool feels like a first-class part of the Operaton ecosystem from the very first screen.

**Acceptance Criteria:**

**Given** the `github.com/operaton/operaton.org` Jekyll source
**When** inspected before `starter-web` implementation begins
**Then** CSS design tokens (colours, typography scale, spacing scale) are extracted and documented; `tailwind.config.js` in `starter-web` maps these tokens as a custom Tailwind theme — colour palette, font families, and spacing values match the operaton.org design system

**Given** `starter-web` with Tailwind configured
**When** any component is inspected
**Then** all Tailwind class names are static strings — no dynamic class construction via template literals or string concatenation; this ensures Tailwind's JIT purge does not remove used classes in production builds

**Given** the app shell
**When** rendered in a browser
**Then** it displays a header and footer visually consistent with `operaton.org`; before Story 4.1 is marked complete, a visual review confirms the app shell matches the operaton.org aesthetic and is not overbloated — a peer screenshot comparison against `start.spring.io` is the acceptance gate

**Given** Vue Router configured with two routes
**When** a user navigates to `/`
**Then** `GalleryView.vue` renders (empty shell at this stage, populated in Story 4.2)

**Given** Vue Router configured
**When** a user navigates to `/configure`
**Then** `ConfigureView.vue` renders (empty shell at this stage, populated in Story 4.3)

**Given** the `useMetadata` composable in `starter-web/src/composables/useMetadata.ts`
**When** the app loads
**Then** it calls `GET /api/v1/metadata` using the OpenAPI-generated client from `src/generated/`; it exposes `{ data: Ref<MetadataResponse | null>, isLoading: Ref<boolean>, error: Ref<ProblemDetail | null> }`

**Given** a shared `<ErrorBanner>` component in `starter-web/src/components/`
**When** any composable exposes a non-null `error`
**Then** `<ErrorBanner>` displays a user-facing error message — no inline error handling exists in individual view components

**Given** Vite build configuration in `starter-web/vite.config.ts`
**When** `npm run build` executes
**Then** build output is written directly to `starter-server/src/main/resources/static/`; running `mvn verify` from the project root produces a Spring Boot JAR containing the latest web build

**Given** `npm run build` and `npm run test:unit` in `starter-web`
**When** executed
**Then** both complete with zero errors and zero lint warnings

### Story 4.2: Project Gallery View

As a **developer exploring Operaton for the first time**,
I want to browse available project types as visual gallery cards with descriptions, tags, and persona hints,
So that I can discover the right project type for my use case without consulting external documentation.

**Acceptance Criteria:**

**Given** a user navigates to `/` (GalleryView)
**When** the page loads
**Then** a hero section is shown at the top with a headline ("Start your Operaton project"), a one-sentence subtitle, and two CTAs: "Configure Now →" (navigates directly to `/configure`, bypassing the gallery) and "Browse Project Types ↓" (scrolls to the gallery cards section); the hero allows Practitioners to skip gallery discovery entirely

**Given** the gallery section below the hero
**When** the page loads
**Then** it fetches metadata from `useMetadata` and renders one card per `projectType` entry; cards display `displayName`, `description`, `tags` as badges, and `personaHint` as a contextual positioning statement; no project type is hardcoded in the component

**Given** metadata is loading
**When** the gallery renders
**Then** skeleton placeholder cards are shown during the loading state — no layout shift when cards appear

**Given** a gallery card
**When** a user clicks it
**Then** they are navigated to `/configure` with the selected `projectType` pre-selected in the form

**Given** the gallery layout
**When** rendered at desktop width
**Then** cards are displayed in a grid; the layout is clean and not overbloated — consistent with the code.quarkus.io extension gallery as the visual reference for discovery UX

**Given** the gallery layout
**When** rendered at mobile width (< 768px)
**Then** the card grid collapses to a single column; cards are full-width; no horizontal scrolling occurs

**Given** a user hovering over a project type card
**When** the `?` help icon is present
**Then** inline contextual help expands explaining the project type in plain language — no navigation away from the page required (covers FR41)

**Given** `useMetadata` returns an error
**When** the gallery renders
**Then** `<ErrorBanner>` is shown; no broken layout or unhandled exception occurs

**Given** the gallery page
**When** navigated to via keyboard (Tab, Enter)
**Then** every card and interactive element is reachable and activatable via keyboard alone; visible focus indicators are present on all focusable elements

### Story 4.3: Configuration Form Rendering

As a **Practitioner who knows exactly what they want to build**,
I want a direct configuration form pre-populated with sensible defaults where I can select project type, build system, and identity fields,
So that I can configure my project without hunting through documentation.

**Acceptance Criteria:**

**Given** a user is on `/configure`
**When** they want to return to the gallery
**Then** a "← Back to gallery" link is visible in the header area and navigates to `/` without losing the current form state in the URL

**Given** a user navigates to `/configure`
**When** the page loads
**Then** the form renders with all configuration options populated from `useMetadata` — no option list is hardcoded; defaults are applied: `projectType=PROCESS_APPLICATION`, `buildSystem=MAVEN`, `javaVersion=17`, `dependencyUpdater=RENOVATE`, `dockerCompose=false`, `githubActions=true`

**Given** a user arriving from the gallery with a pre-selected project type
**When** the form renders
**Then** the `projectType` field is pre-selected to match the gallery selection; all other fields use defaults

**Given** `projectType=PROCESS_ARCHIVE` is selected
**When** the form updates
**Then** a `deploymentTarget` selector appears with a smooth transition; it is absent for all other project types

**Given** the form layout
**When** rendered at desktop width
**Then** it follows a clean single-column or two-column structure with clear visual grouping of related fields (identity, build options, extras) — `start.spring.io` is the visual benchmark; no sprawling multi-panel layout; related fields are grouped with `<fieldset>` + `<legend>` for semantic structure; all labels are positioned above their inputs (no floating labels or placeholder-as-label patterns)

**Given** the form layout
**When** rendered at mobile width (< 768px)
**Then** the layout is a single column with the form above and the preview panel collapsed to a `<details>` disclosure element below; the full form is accessible without horizontal scrolling

**Given** any configuration option on the form (project type, build system, Java version, deployment target, dependency updater, Docker Compose, GitHub Actions)
**When** the user clicks or hovers over its `?` help icon
**Then** inline contextual help text expands explaining the option in plain language — without leaving the page; every configuration field has a help icon (covers FR20, UX-DR3)

**Given** a required field is empty or invalid (e.g. `groupId` with spaces, `artifactId` not matching the spec pattern)
**When** the user interacts with the field
**Then** client-side validation displays inline error messages; field constraints and regex patterns mirror the OpenAPI spec exactly — no independently invented validation rules

**Given** the configuration form
**When** navigated entirely by keyboard
**Then** every field, selector, and toggle is reachable via Tab and operable via keyboard; visible focus rings are present throughout

### Story 4.4: Project Generation & Download

As a **Practitioner who has configured their project**,
I want to click "Generate & Download" and receive my ZIP in under 30 seconds,
So that I can move immediately from configuration to working with my new project.

**Acceptance Criteria:**

**Given** all required fields are filled and valid
**When** the user clicks "Generate & Download"
**Then** the form calls `POST /api/v1/generate` via `useGenerate` composable; on success the browser triggers a ZIP download named `{artifactId}.zip` using `URL.createObjectURL` and a temporary anchor element — not `window.open` or a redirect; `isGenerating` is `true` during the request and `false` after

**Given** a Practitioner who knows their inputs
**When** they land on `/configure` with defaults and fill in their identity fields
**Then** a timed walkthrough from page load to ZIP download completing finishes in under 30 seconds — this is a manual acceptance gate before the story is marked complete

**Given** the `useGenerate` composable
**When** inspected
**Then** it exposes `{ data: Ref<Blob | null>, isLoading: Ref<boolean>, error: Ref<ProblemDetail | null> }` — consistent composable shape per architecture

**Given** the API returns an error response
**When** generation fails
**Then** `<ErrorBanner>` displays the Problem Details message; the form remains filled so the user can correct and retry without re-entering all fields

**Given** generation succeeds
**When** the ZIP download starts
**Then** a transient success message "Downloaded {artifactId}.zip" is shown to confirm the download initiated; the message disappears automatically after a short delay

**Given** the "Generate & Download" button
**When** navigated by keyboard
**Then** it is reachable via Tab and triggerable via Enter; a visible loading state is shown during generation

### Story 4.5: Live File Tree Preview

As a **developer configuring a project**,
I want to see a live file tree preview that updates as I change configuration options,
So that I know exactly what files will be generated before I download the ZIP.

**Acceptance Criteria:**

**Given** the configuration form with a project type and build system selected
**When** the form renders
**Then** a file tree preview panel is visible alongside the form, populated from `metadata.projectTypes[selected].templateManifest` for the current configuration — no server round-trip is made per configuration change

**Given** a user changes any configuration option (e.g. switches build system, toggles Docker Compose)
**When** the change is applied
**Then** the file tree preview updates within 200ms; files conditioned on the changed option appear or disappear immediately; the update is a pure client-side computation — `GET /api/v1/metadata` is called only once on page load

**Given** `templateManifest` entries with a non-null `condition`
**When** the preview renders
**Then** a file is shown only if its condition evaluates to `true` given the current form state (e.g. `docker-compose.yml` shown only when `dockerCompose=true`); condition evaluation is a pure function of form state and manifest data

**Given** the preview panel
**When** a file path is displayed
**Then** it reflects identity propagation — e.g. the BPMN file name shows `{artifactId}.bpmn`, the Java package path shows `{groupId}/{artifactId}/`, using the current form values

**Given** the preview is a pure function of metadata and form state
**When** tested in Vitest
**Then** a unit test covers: switching build system changes the build file shown, toggling `dockerCompose` shows/hides `docker-compose.yml`, toggling `githubActions` shows/hides `.github/workflows/ci.yml`, identity values propagate into displayed file names

**Given** the preview panel layout
**When** rendered at desktop width
**Then** it is visually distinct from the form but does not dominate the layout — clean, readable, not overbloated; consistent with how `start.spring.io` presents its file tree

**Given** the preview panel
**When** rendered at mobile width (< 768px)
**Then** it is shown as a `<details>` disclosure element below the form with summary text "File Structure Preview"; it is collapsed by default and expands on click; the file tree is still fully navigable when expanded

### Story 4.6: Download, IDE Deep-Links & Shareable Config URLs

As a **developer who has configured their project**,
I want to open the generated project directly in my IDE and share my configuration with teammates as a URL,
So that I save time on import steps and colleagues can reproduce my exact setup instantly.

**Acceptance Criteria:**

**Given** a form with all required fields valid
**When** the configuration is complete (no prior download required)
**Then** an IntelliJ IDEA deep-link button and a VS Code deep-link button are shown in an action panel alongside the file tree preview; clicking the IntelliJ button constructs and opens `idea://com.intellij.ide.starter?url={encoded_generate_url}` where `{encoded_generate_url}` is the full `POST /api/v1/generate` URL with form state encoded as query params; clicking the VS Code button opens `vscode://vscjava.vscode-spring-initializr/open?url={encoded_generate_url}` with the same encoding scheme

**Given** an IDE deep-link button is clicked
**When** the IDE handles the URL scheme
**Then** the IDE downloads the ZIP from `POST /api/v1/generate` (using the encoded configuration params) and imports the project — no file is required in the browser's Downloads folder; the service remains stateless

**Given** a user has configured the form with non-default values
**When** they click the "Copy Shareable Link" button in the action panel
**Then** the full URL with query parameters encoding the current configuration is copied to the clipboard; a brief confirmation ("Link copied!") is shown; opening this URL in a new tab restores and pre-fills the form with the same configuration

**Given** a shareable config URL is opened
**When** the page loads
**Then** the form is pre-filled from URL query parameters before the user interacts with it; `useMetadata` is still called to populate option lists — query params only set selected values, not option lists

**Given** a shareable URL with an invalid or unknown parameter value
**When** the form loads
**Then** the invalid value is silently ignored and the default for that field is applied — no error state, no broken layout

**Given** the share URL
**When** inspected
**Then** it uses only URL-safe characters and is human-readable enough to identify the configuration at a glance (e.g. `?projectType=PROCESS_APPLICATION&buildSystem=GRADLE_KOTLIN`)

### Story 4.7: Keyboard Navigation & WCAG 2.1 AA Accessibility

As a **developer who relies on keyboard navigation or assistive technology**,
I want the full configuration and download flow to be operable without a mouse and to meet WCAG 2.1 AA standards,
So that the tool is inclusive and accessible to all developers regardless of input method.

**Acceptance Criteria:**

**Given** the app shell
**When** rendered
**Then** a visually hidden skip link "Skip to main content" is the first focusable element; it becomes visible on keyboard focus and navigates to the `#main-content` landmark, bypassing the header navigation

**Given** the full user journey from `/` (gallery) to `/configure` (form) to "Generate & Download"
**When** navigated using only Tab, Shift+Tab, Enter, Space, and arrow keys
**Then** every interactive element is reachable and operable; no step in the journey requires a mouse; the tab order is logical and follows the visual layout

**Given** any focusable element (button, input, link, card)
**When** it receives keyboard focus
**Then** a visible focus ring is displayed using the operaton.org brand colour; the focus ring meets WCAG 2.1 AA minimum contrast requirements

**Given** all form inputs, buttons, and gallery cards
**When** inspected
**Then** every element has an appropriate ARIA label or accessible name; images and icons have descriptive `alt` text or `aria-label`; no interactive element relies on colour alone to convey state

**Given** the axe-core accessibility validator is integrated into the `lint-web` CI job
**When** the job runs on every PR
**Then** zero WCAG 2.1 AA violations are reported for the gallery view and configuration form; any violation blocks merge

**Given** the live preview panel updates dynamically
**When** a screen reader user changes a configuration option
**Then** the preview region is marked with `aria-live="polite"` so changes are announced without interrupting the current focus

**Given** error messages displayed by `<ErrorBanner>` or inline field validation
**When** they appear
**Then** they are announced by screen readers via `role="alert"` or `aria-live="assertive"`; focus is not forcibly moved away from the current field

---

## Epic 5: CLI & MCP — Terminal and AI-Native Access

Developers generate projects from `npx operaton-starter` in scriptable or interactive mode. AI assistants (Claude, GitHub Copilot, Cursor) generate projects mid-conversation via the `operaton-starter-mcp` npm tool. All four access channels are live.

### Story 5.1: CLI — `npx operaton-starter` (Flag Mode & Pipe Mode)

As a **developer working in a terminal or writing shell scripts**,
I want to generate an Operaton project archive using `npx operaton-starter` with command-line flags,
So that I can automate project generation in scripts and CI pipelines without opening a browser.

**Acceptance Criteria:**

**Given** `starter-cli` is a new Maven module in the monorepo
**When** inspected
**Then** it uses `frontend-maven-plugin` with the same pinned Node.js/npm versions as `starter-mcp`; it is published as the `operaton-starter` npm package (distinct from `operaton-starter-mcp`); its `src/generated/` contains the OpenAPI-generated API client generated independently from the same `openapi.yaml`

**Given** a developer runs `npx operaton-starter` with the full flag set: `--groupId`, `--artifactId`, `--projectName`, `--projectType`, `--buildSystem`, `--javaVersion`, `--deploymentTarget`, `--dependencyUpdater`, `--dockerCompose`, `--githubActions`
**When** stdout is a terminal
**Then** the CLI downloads the generated ZIP and saves it to the current directory as `{artifactId}.zip`; a success message is printed to stdout

**Given** a developer runs `npx operaton-starter [flags] > my-app.zip`
**When** stdout is a pipe
**Then** the CLI outputs raw ZIP bytes to stdout with no other output; all status output goes to stderr only

**Given** the `--output <dir>` flag is provided
**When** generation succeeds
**Then** the CLI extracts the ZIP into the specified directory; the directory is created if it does not exist

**Given** the `--extract` flag is provided without `--output`
**When** generation succeeds
**Then** the CLI extracts the ZIP into a directory named `{artifactId}` in the current working directory

**Given** the CLI implementation
**When** inspected
**Then** it uses the OpenAPI-generated client from `src/generated/` to call `POST /api/v1/generate`; no hand-written HTTP client code exists; no generation logic is duplicated from `starter-templates`; interactive/prompt mode is explicitly out of scope for MVP — flag-only

**Given** the CLI base URL
**When** not overridden
**Then** it defaults to `https://start.operaton.org`; overridable via `OPERATON_STARTER_URL` environment variable for self-hosted instances

**Given** a required flag is missing
**When** the CLI is invoked
**Then** it prints a clear usage error to stderr and exits with a non-zero exit code; no partial generation is attempted

**Given** the API returns an error
**When** the CLI handles it
**Then** the Problem Details error message is printed to stderr; the CLI exits with a non-zero exit code

**Given** `npm publish` for the `starter-cli` package
**When** executed via the `npm-publish` CI job on a tagged release
**Then** it publishes as `operaton-starter` on npm — enabling `npx operaton-starter` without prior installation

### Story 5.2: MCP npm Package — `operaton-starter-mcp`

As a **developer using an AI coding assistant** (Claude, GitHub Copilot, Cursor),
I want to generate an Operaton project mid-conversation by asking my AI assistant,
So that I receive a ready-to-use project archive without leaving my development environment or opening a browser.

**Acceptance Criteria:**

**Given** the `starter-mcp` module
**When** inspected
**Then** it exposes a single MCP tool named `generate_project`; the tool's input schema is generated from `openapi.yaml` — no hand-written schema definition exists independently of the API contract

**Given** an AI assistant with `operaton-starter-mcp` registered
**When** it invokes `generate_project` with a valid configuration
**Then** the MCP tool calls `POST /api/v1/generate` via the OpenAPI-generated client and returns the ZIP as a response that the AI assistant can present to the user

**Given** the MCP package base URL
**When** not overridden
**Then** it defaults to `https://start.operaton.org`; overridable via `OPERATON_STARTER_URL` environment variable — enabling use against a self-hosted instance (FR32)

**Given** the AI assistant invokes `generate_project` with an invalid configuration
**When** the API returns a `400` Problem Details response
**Then** the MCP tool returns the error detail as a readable string to the AI assistant — enabling the assistant to explain the error and suggest corrections

**Given** `npm publish` for the `starter-mcp` package
**When** executed via the `npm-publish` CI job on a tagged release
**Then** it publishes as `operaton-starter-mcp` on npm with correct `main` (compiled JS) and `types` (`.d.ts`) fields; the package is independently versioned from `starter-cli`

**Given** an AI assistant user registers `operaton-starter-mcp` and asks "generate a Process Application with Gradle Kotlin DSL, group com.acme, artifact loan-approval"
**When** the assistant invokes `generate_project`
**Then** it returns a valid ZIP for the described configuration — this end-to-end scenario is the acceptance gate for the story

---

## Epic 6: Self-Hosting & Production Operations

Platform engineers and enterprise teams run a private Operaton Starter instance behind their firewall — configured entirely via environment variables, deployed from a published Docker image, with zero external dependencies at startup and automated release pipelines.

### Story 6.1: Docker Image — Build, Configure & Publish

As a **platform engineer deploying Operaton Starter behind a firewall**,
I want a published Docker image that starts with zero external network calls and runs identically to the public instance,
So that my team can run a private Operaton Starter instance with no SaaS dependencies.

**Acceptance Criteria:**

**Given** the `Dockerfile` at the project root
**When** inspected
**Then** it uses `eclipse-temurin:25-jre-alpine` as the base image; it uses Spring Boot's layered JAR extraction to separate dependencies, Spring Boot internals, and application code into distinct Docker layers — maximising cache reuse on incremental builds; it exposes port 8080; no secrets or environment-specific values are baked into the image

**Given** `docker build` runs after `mvn verify`
**When** the build completes
**Then** the image is produced with zero errors; the image contains the Spring Boot fat JAR with the compiled `starter-web` assets embedded at `BOOT-INF/classes/static/`

**Given** `docker run operaton/operaton-starter`
**When** the container starts
**Then** it makes zero outbound network calls at startup; `GET /actuator/health` returns `200 OK { "status": "UP" }` within 30 seconds — verified by a CI smoke test that polls the endpoint and fails if not ready within that window

**Given** the running Docker container is started then put into network-isolated mode (`--network none`)
**When** `GET /actuator/health` is called
**Then** it returns `200 OK` — proving the service has no runtime external network dependency

**Given** the running Docker container
**When** `POST /api/v1/generate` is called with a valid configuration
**Then** it returns a valid ZIP — the full generation pipeline works inside the container with no external dependencies

**Given** the `docker-compose.dev.yml` at the project root
**When** `docker compose -f docker-compose.dev.yml up` is run by a contributor
**Then** the service starts and is accessible at `http://localhost:8080`

**Given** a tagged release (e.g. `v1.0.0`)
**When** the `docker-publish` CI job runs
**Then** it builds the image and pushes it to `docker.io/operaton/operaton-starter` with both the version tag (e.g. `1.0.0`) and `latest`; no image is pushed on non-tagged commits

**Given** the published image on Docker Hub
**When** an operator runs `docker pull operaton/operaton-starter`
**Then** the image is publicly accessible without authentication

### Story 6.2: Environment Variable Configuration & Self-Hosting Validation

As a **platform engineer running a private Operaton Starter instance**,
I want to configure default values and registry URLs via environment variables,
So that generated projects automatically use my organisation's standards without developers needing to override anything.

**Acceptance Criteria:**

**Given** `starter-server` Spring Boot configuration
**When** inspected
**Then** all self-hosting defaults are bound via `@ConfigurationProperties` with the `starter.defaults.*` and `starter.cors.*` namespaces; no custom environment variable parsing exists; Spring's relaxed binding maps env vars to properties automatically

**Given** the Docker container is started with `DEFAULT_GROUP_ID=com.bank`
**When** `GET /api/v1/metadata` is called
**Then** the metadata response includes `defaultGroupId: "com.bank"` so that the web UI, CLI, and MCP can pre-fill the `groupId` field with the org default; a developer can still override it

**Given** the Docker container is started with `MAVEN_REGISTRY=https://nexus.bank.internal/repository/maven-public`
**When** a project is generated
**Then** the generated `pom.xml` or Gradle build file references the configured Maven registry URL; no reference to Maven Central appears when a custom registry is configured

**Given** the Docker container is started with `OPERATON_VERSION=2.0.0`
**When** a project is generated
**Then** the generated project targets the specified pinned Operaton version; the public instance at `start.operaton.org` does not expose this override — it always uses the BOM version baked at build time; `OPERATON_VERSION` is a self-hosted-only operator configuration

**Given** the Docker container is started with `CORS_ALLOWED_ORIGINS=https://start.operaton.internal`
**When** a browser at `https://start.operaton.internal` calls the API
**Then** the CORS response headers permit the request; requests from unlisted origins are rejected

**Given** the Docker container is started with no environment variables
**When** `POST /api/v1/generate` is called
**Then** generation succeeds using built-in defaults — no env var is required for the service to function

**Given** the Klaus self-hosting acceptance scenario
**When** an operator runs the image with `DEFAULT_GROUP_ID=com.bank`, `MAVEN_REGISTRY=https://nexus.bank.internal/repository/maven-public`, and `OPERATON_VERSION=2.0.0`
**Then** the web UI shows `com.bank` pre-filled in the `groupId` field; a generated `pom.xml` references the Nexus registry; the generated project targets Operaton 2.0.0 — this three-env-var end-to-end scenario is the manual acceptance gate for this story

**Given** the Docker image documentation (README or `docker-compose.dev.yml`)
**When** inspected
**Then** all supported environment variables are listed with their default values and descriptions; no undocumented env vars affect behaviour
Platform engineers and enterprise teams run a private Operaton Starter instance behind their firewall — configured entirely via environment variables, deployed from a published Docker image, with zero external dependencies at startup and automated release pipelines.
**FRs covered:** FR37–38
**ARCH covered:** ARCH-9, ARCH-10
**NFRs:** NFR5, NFR6, NFR19
