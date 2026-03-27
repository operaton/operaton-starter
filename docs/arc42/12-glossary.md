# Arc42 Section 12: Glossary

## Domain Terms

| Term | Definition |
|------|-----------|
| **Operaton** | Open-source BPMN process engine; fork of Camunda 7; hosted at `operaton.org` |
| **Operaton BOM** | Bill of Materials POM (`operaton-bom`) that provides dependency management for all Operaton artifacts; generated projects always target the current stable version |
| **Process Application** | A Spring Boot application embedding the Operaton engine. Includes a skeleton BPMN process, a Java delegate stub, and full Spring Boot configuration. The default project type for developers building new Operaton-powered applications. |
| **Process Archive** | An engine-agnostic deployable artifact (WAR or JAR) for deployment to a Standalone Engine. Contains `processes.xml` and BPMN resources but no embedded engine. Used when an organisation runs a shared Operaton server (Tomcat or Standalone). |
| **DeploymentTarget** | Enum specifying where a Process Archive will run: `TOMCAT` or `STANDALONE_ENGINE`. Relevant only for the `PROCESS_ARCHIVE` project type. |
| **Identity Propagation** | The invariant that `groupId`, `artifactId`, and `projectName` from `ProjectConfig` flow into all generated files: `pom.xml`/`build.gradle`, Java package names, BPMN process ID, and `spring.application.name`. |
| **BPMN** | Business Process Model and Notation. The XML-based standard for defining executable process models. Generated projects include a skeleton `.bpmn` file. |
| **JavaDelegate** | Operaton service task implementation interface. Generated projects include a `SkeletonDelegate.java` stub wired to the skeleton BPMN task. |
| **Camunda 7 Migration** | Phase 2 project type. Generates a migration scaffold with OpenRewrite recipe integration and a `MIGRATION.md` checklist for teams migrating from Camunda 7 to Operaton. |

## Architecture Terms

| Term | Definition |
|------|-----------|
| **GenerationEngine** | The single public API in `starter-templates`: `GenerationEngine.generate(ProjectConfig) → byte[]`. The only code that produces project archive ZIPs. All channels invoke this (directly or via REST). |
| **ProjectConfig** | Java record in `org.operaton.dev.starter.templates.model` that carries all configuration for a generation request. The domain model. Not the same as the generated API DTO. |
| **MetadataProvider** | Component in `starter-templates` that provides project type descriptors and template manifests. The source of truth for `GET /api/v1/metadata`. |
| **TemplateManifest** | A flat list of `{ path, condition, templateId }` entries per project type. Enables client-side file tree preview without server round-trips. Part of the `GET /api/v1/metadata` response. |
| **GenerationClient** | Strategy interface in `starter-archetypes`. MVP: `RestGenerationClient` (HTTP). Phase 2: `EmbeddedGenerationClient` (direct in-process call). |
| **Spec freeze** | The point at which `openapi.yaml` is committed and client generation begins. Post-freeze changes require regenerating all clients. Enforced by CI `contract-check` job. |
| **Channel** | Any consumer of the generation engine: web UI (`starter-web`), CLI (`starter-cli`), MCP (`starter-mcp`), REST API (direct curl/HTTP), `mvn archetype:generate` (`starter-archetypes`). All channels use the same engine. |
| **Metadata contract** | `GET /api/v1/metadata` is the projection between the engine and all channels. No channel hardcodes option lists — all option data comes from this endpoint. |

## Technology Terms

| Term | Definition |
|------|-----------|
| **JTE** | Java Template Engine. Precompiles templates to Java classes at build time. Zero runtime template parsing. Used in `starter-templates`. |
| **jte-maven-plugin** | Maven plugin that compiles JTE templates to Java classes during the `generate-sources` phase. |
| **openapi-generator** | Tool that generates server stubs (Java/Spring) and client code (TypeScript) from `openapi.yaml`. Used in all modules that consume the API. |
| **Scalar** | API documentation renderer. Displays `openapi.yaml` as an interactive docs page at `/api/v1/docs`. Loaded from CDN via a static HTML page. |
| **Bucket4j** | Java rate limiting library. Used in `starter-server` for in-memory, per-IP rate limiting. No Redis dependency. |
| **RFC 7807** | "Problem Details for HTTP APIs". Standard format for HTTP error responses (`application/problem+json`). Used for all error responses from `starter-server`. |
| **ArchUnit** | Java testing library for architecture constraints. Used in `ZeroSpringDependencyTest` to enforce zero Spring imports in `starter-templates`. |
| **frontend-maven-plugin** | Maven plugin (`com.github.eirslett:frontend-maven-plugin`) that downloads a pinned Node.js/npm version and runs npm scripts. Used in `starter-web`, `starter-mcp`, and `starter-cli` Maven modules for hermetic builds. |
| **Vite** | Build tool for `starter-web`. Produces optimized static assets copied to `starter-server/src/main/resources/static/`. |
| **Tailwind CSS** | Utility-first CSS framework used in `starter-web`. Configured with operaton.org design tokens extracted from the Jekyll source. |
| **MCP** | Model Context Protocol. Enables AI assistants (Claude, GitHub Copilot, Cursor) to call tools programmatically. `starter-mcp` exposes a `generate_project` MCP tool. |
| **Logstash Logback Encoder** | Logback encoder that formats log output as structured JSON. Used in `starter-server` for machine-readable logs. |
| **eclipse-temurin** | Community-maintained OpenJDK distribution. Used as the Docker base image (`eclipse-temurin:25-jre-alpine`). |
| **OpenRewrite** | Automated code refactoring tool. `operaton/migrate-from-camunda-recipe` provides OpenRewrite recipes for Camunda 7 → Operaton migration. Phase 2 dependency. |
| **axe-core** | Accessibility testing engine. Used in the CI `lint-web` job to enforce WCAG 2.1 AA compliance. Hard block on violations. |
| **Spring Boot Actuator** | Spring Boot module providing the `/actuator/health` endpoint used by load balancers for health checks. |
| **Layered JAR** | Spring Boot feature that splits the application JAR into layers (dependencies, spring-boot-loader, snapshot-dependencies, application) for Docker layer cache efficiency. |

## Personas

| Persona | Description |
|---------|------------|
| **Marcus (Practitioner)** | Senior Operaton developer who knows what he wants. Uses the form view. Completes in under 30 seconds. |
| **Elena (Migrator)** | Camunda 7 architect evaluating migration to Operaton. Uses the gallery migration card and `MIGRATION.md` output. Phase 2 persona. |
| **Thomas (Newcomer / Explorer)** | Spring Boot developer new to BPM. Uses the gallery, inline contextual help, and live preview to discover the right project type. |
| **Priya (API Consumer)** | Platform engineer integrating operaton-starter into a Backstage developer portal. Uses REST API, MCP module, and self-hosted Docker. |
| **Klaus (Admin)** | Operator running a self-hosted instance on-premises. Uses Docker image with env-var configuration. Zero external dependencies. |

## Build System Abbreviations

| Abbreviation | Full Name |
|-------------|-----------|
| `MAVEN` | Apache Maven (with `pom.xml`) |
| `GRADLE_GROOVY` | Gradle with Groovy DSL (`build.gradle`) |
| `GRADLE_KOTLIN` | Gradle with Kotlin DSL (`build.gradle.kts`) |

## Environment Variables

| Variable | Scope | Description |
|----------|-------|-------------|
| `DEFAULT_GROUP_ID` | Self-hosted | Pre-fills Group ID field; injected into generated project coordinates |
| `MAVEN_REGISTRY` | Self-hosted | Maven repository URL injected into generated `pom.xml` / `build.gradle` |
| `STARTER_DEFAULTS_OPERATON_VERSION` | Self-hosted only | Pins Operaton version; public instance uses version baked at build time |
| `CORS_ALLOWED_ORIGINS` | Self-hosted | Additional CORS origins beyond `start.operaton.org` and `localhost` |
| `OPERATON_STARTER_URL` | CLI, MCP | Override base URL for CLI and MCP clients (default: `https://start.operaton.org`) |
