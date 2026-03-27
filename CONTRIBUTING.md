# Contributing to operaton-starter

Thank you for your interest in contributing. This document covers setup, project structure, and contribution guidelines.

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java | 21+ | Required to build the server and templates modules |
| Maven | 3.9+ | Or use the `./mvnw` wrapper |
| Node.js | Active LTS | Only needed if working on `starter-web`, `starter-mcp`, or `starter-cli` directly; Maven hermetically downloads Node via `frontend-maven-plugin` |
| Docker | Any recent | For running the full stack locally |

## Building

**Full build (all 6 modules):**
```bash
./mvnw verify
```

**Server only (faster for backend work):**
```bash
./mvnw verify -pl starter-templates,starter-server
```

**Web UI only:**
```bash
cd starter-web && npm ci && npm run dev
# Requires backend running — see Local Development below
```

## Local Development

Start the backend with Docker Compose, then run the Vite dev server:

```bash
# Terminal 1: start backend
docker compose -f docker-compose.dev.yml up

# Terminal 2: start frontend with hot reload
cd starter-web && npm ci && npm run dev
```

The Vite dev server proxies API requests to `localhost:8080`. Open `http://localhost:5173`.

## Project Structure

```
operaton-starter/
├── openapi.yaml             API contract — edit this, then regenerate clients
├── starter-templates/       Pure-Java generation engine (zero Spring dependency)
├── starter-server/          Spring Boot REST API
├── starter-archetypes/      mvn archetype:generate integration
├── starter-web/             Vue 3 SPA
├── starter-mcp/             MCP npm package
├── starter-cli/             CLI npm package
└── docs/
    ├── arc42/               Architecture documentation (12 sections)
    └── bmad/                Planning artifacts (PRD, architecture decisions, epics)
```

For a full description of every file and module, see [`docs/arc42/05-building-block-view.md`](docs/arc42/05-building-block-view.md).

## Key Rules

### Never edit generated files

`src/generated/` directories in TypeScript modules and `target/generated-sources/` in Java modules are owned by `openapi-generator`. To change API types:

1. Edit `openapi.yaml`
2. Run `./mvnw generate-sources` (Java) or `npm run generate` (TypeScript)
3. Commit both `openapi.yaml` and the regenerated files

### Keep `starter-templates` free of Spring

`starter-templates` is a pure-Java library. It must not import anything from `org.springframework.*`. This is enforced by `ZeroSpringDependencyTest` (ArchUnit) — the build will fail if you accidentally add a Spring dependency.

### Domain model lives in `starter-templates`

`ProjectConfig`, `ProjectType`, `BuildSystem`, and `DeploymentTarget` are defined once in `org.operaton.dev.starter.templates.model`. Do not redefine these types in any other module.

### Add tests for new project types and config fields

- A new project type requires a new `@ParameterizedTest` entry in `GenerationEngineTest`
- A new config field requires updating `useShareableLink.ts` and its round-trip Vitest test

## Adding a New Project Type

1. Add an enum value to `ProjectType` in `starter-templates`
2. Create JTE templates under `src/main/jte/{project-type}/maven/`, `gradle-groovy/`, `gradle-kotlin/`
3. Add a `ProjectTypeDescriptor` entry in `MetadataProvider` (with `templateManifest`)
4. Extend `GenerationEngineTest` with `@ParameterizedTest` entries for all 3 build systems
5. The CI test matrix picks up new combinations automatically

## Running Tests

**All tests:**
```bash
./mvnw verify
```

**Generation engine only (fast, no Spring context):**
```bash
./mvnw test -pl starter-templates
```

**Web UI unit tests:**
```bash
cd starter-web && npm run test
```

**Accessibility audit (axe-core):**
```bash
cd starter-web && npm run test:a11y
```

## CI Pipeline

Every pull request runs 4 parallel jobs:

| Job | What it checks |
|-----|----------------|
| `build-java` | `mvn verify` — compilation, unit tests, ArchUnit |
| `test-matrix` | 6 parallel generation + smoke-test jobs (2 types × 3 build systems) |
| `contract-check` | Regenerated clients match committed `openapi.yaml` (warning level) |
| `lint-web` | ESLint, Vitest, axe-core WCAG 2.1 AA audit |

All jobs must pass before merge (except `contract-check`, which is warning-level in Phase 1).

## Commit Style

Follow conventional commits: `feat:`, `fix:`, `docs:`, `test:`, `chore:`, `refactor:`.

Examples:
```
feat(templates): add Gradle Kotlin DSL support for process archive
fix(server): return 400 on unknown buildSystem value
docs(arc42): update runtime view with MCP tool call scenario
test(templates): extend parameterized test to cover all 6 combinations
```

## Architecture Questions

See the full architecture documentation in [`docs/arc42/`](docs/arc42/). Key sections for contributors:

- [`04-solution-strategy.md`](docs/arc42/04-solution-strategy.md) — why JTE, OpenAPI spec-first, Tailwind
- [`08-crosscutting-concepts.md`](docs/arc42/08-crosscutting-concepts.md) — naming, error handling, testing patterns
- [`09-architecture-decisions.md`](docs/arc42/09-architecture-decisions.md) — ADRs for all significant decisions

## Reporting Issues

Please open an issue on GitHub with:
- The channel used (web UI / CLI / curl / MCP)
- For generated project issues: the project type and build system
- For server issues: the request body and response (redact any sensitive data)
