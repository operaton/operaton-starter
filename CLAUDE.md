# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Operaton Starter is a multi-channel project generator for the Operaton BPM ecosystem — similar to Spring Initializr, but for BPMN/DMN projects. All generation channels (web UI, REST API, CLI, Maven archetype) converge to a single entry point: `GenerationEngine.generate(ProjectConfig) → byte[]` in the `starter-templates` module.

## Build Commands

```bash
# Full build (all modules)
./mvnw verify

# Backend only (faster iteration)
./mvnw verify -pl starter-templates,starter-server -am

# Web UI development server
cd starter-web && npm ci && npm run dev

# Web UI production build
cd starter-web && npm run build
```

## Test Commands

```bash
# All Java tests
./mvnw verify

# Generation engine only (no Spring context, fastest)
./mvnw test -pl starter-templates

# Single Java test class
./mvnw test -pl starter-templates -Dtest=GenerationEngineTest

# Single Java test method
./mvnw test -pl starter-templates -Dtest=GenerationEngineTest#generate_returns_valid_zip

# Web UI unit tests
cd starter-web && npm run test:unit

# Single web UI test file
cd starter-web && npm run test:unit -- useShareableLink.test.ts

# Web UI accessibility audit (WCAG 2.1 AA via axe-core)
cd starter-web && npm run test:a11y

# Web UI lint
cd starter-web && npm run lint
```

## Module Architecture

This is a 5-module Maven monorepo. The dependency direction is: everything depends on `starter-templates`; nothing in `starter-templates` depends on Spring.

### `starter-templates` (Pure Java, zero Spring)
The core domain and generation engine. Key types:
- `ProjectConfig` — the central domain object carrying all generation parameters
- `ProjectType` — `PROCESS_APPLICATION`, `PROCESS_ARCHIVE`, `DMN_PROJECT`
- `BuildSystem` — `MAVEN`, `GRADLE_GROOVY`, `GRADLE_KOTLIN`
- `DeploymentTarget` — `TOMCAT`, `STANDALONE_ENGINE`
- `GenerationEngine` — the single entry point for all generation; takes `ProjectConfig`, returns a ZIP as `byte[]`
- `MetadataProvider` — provides project descriptors and template manifests

Templates are JTE files, precompiled at build time for production and loaded from the filesystem in development. The ArchUnit test `ZeroSpringDependencyTest` enforces the Spring-free constraint.

### `starter-server` (Spring Boot 4.0.4, Java 21)
REST API layer. Controllers are generated from `openapi.yaml` (root level). Key components:
- `GenerationService` — maps API DTOs to `ProjectConfig` and calls `GenerationEngine`
- `MetadataService` — exposes project metadata
- `GlobalExceptionHandler` — `@ControllerAdvice` producing RFC 7807 Problem Details responses
- Rate limiting via Bucket4j (per-IP token buckets, configurable via `RATE_LIMIT_REQUESTS_PER_MINUTE`)
- Examples gallery — fetches manifests from GitHub repos, caches ZIP archives locally
- All self-hosting configuration lives in `StarterProperties` and can be overridden via environment variables

### `starter-web` (Vue 3 + Vite + Tailwind CSS)
Dual UI paths: **Practitioner** (form-first) and **Explorer** (gallery-first). The OpenAPI-generated client lives in `src/generated/` — never edit those files manually. Build system selection is a two-step UI: first Maven vs. Gradle, then DSL choice for Gradle.

### `starter-cli` (npm: `operaton-starter`)
Entry: `npx operaton-starter`. Dual-mode: piping to stdout (scriptable) or interactive TTY via Commander.

### `starter-archetypes`
Maven archetype integration. Uses a `GenerationClient` strategy interface; currently implemented by `RestGenerationClient`.

## API Contract

`openapi.yaml` at the repo root is the **single source of truth** for the API. It generates:
- Spring server stubs in `starter-server`
- TypeScript clients in `starter-web`, `starter-cli`

Never hand-edit files in `target/generated-sources/` or `src/generated/`. To change the API, edit `openapi.yaml` and regenerate.

Two endpoints:
- `GET /api/v1/generate` — query-parameter mode (for IDE deep-links/shareable URLs)
- `POST /api/v1/generate` — JSON body mode

Both return a ZIP archive.

## Key Environment Variables (server)

| Variable | Purpose |
|---|---|
| `STARTER_DEFAULTS_GROUP_ID` | Pre-fill groupId in forms |
| `STARTER_DEFAULTS_OPERATON_VERSION` | Pin Operaton version |
| `STARTER_CORS_ALLOWED_ORIGINS` | CORS allowlist |
| `STARTER_EXAMPLES_REPOSITORIES` | GitHub repos for examples gallery |
| `RATE_LIMIT_REQUESTS_PER_MINUTE` | Rate limiting (default: 60) |

## Commit Style

Conventional commits: `feat:`, `fix:`, `docs:`, `test:`, `chore:`, `refactor:`

Example: `feat(templates): add Gradle Kotlin DSL support for process archive`

## CI Pipeline

Four parallel jobs run on every PR:
1. `build-java` — `mvn verify` (compile, unit tests, ArchUnit)
2. `test-matrix` — 6 parallel smoke tests (2 project types × 3 build systems)
3. `contract-check` — verifies regenerated clients match committed `openapi.yaml`
4. `lint-web` — ESLint, Vitest, axe-core a11y audit
