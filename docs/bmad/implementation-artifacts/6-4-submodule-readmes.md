---
baseline_commit: 63e1a76a62d421936a5bcad69596371c124781e2
---

# Story 6.4: Submodule READMEs

## Status
done

## Story

As a **new contributor to operaton-starter**,
I want each submodule to have its own `README.md` covering role, prerequisites, build, and run instructions,
So that I can build and exercise any submodule in isolation without consulting other documentation sources.

## Acceptance Criteria

1. **Given** each of the five submodules **When** their root directories are inspected **Then** each contains a `README.md` with sections: Role, Prerequisites, Build in isolation, Run/Use locally, Example

2. **Given** `starter-templates/README.md` **When** inspected **Then** the example section shows a Java code snippet invoking `GenerationEngine.generate(config)` in-process and asserting the returned ZIP is non-empty

3. **Given** `starter-server/README.md` **When** inspected **Then** the run section includes `mvn spring-boot:run -pl starter-server -am` and a curl example calling `POST /api/v1/generate` and saving the result to a file

4. **Given** `starter-web/README.md` **When** inspected **Then** the run section includes `npm run dev` and notes that the Vite dev server proxies API calls to `http://localhost:8080`; references `docker-compose.dev.yml` for the backend

5. **Given** `starter-mcp/README.md` **When** inspected **Then** the example section contains a complete Claude Desktop MCP config JSON snippet with `OPERATON_STARTER_URL` documented

6. **Given** `starter-archetypes/README.md` **When** inspected **Then** it documents the module's role and how to build it in isolation; includes a note that its main purpose is the Maven archetype integration for the generation engine

7. **Given** a contributor who has never read the project root README **When** they follow any single submodule README **Then** they can successfully build and exercise that submodule in isolation

## Tasks/Subtasks

- [x] Task 1: Create `starter-templates/README.md`
  - [x] 1.1: Role section: pure-Java generation engine, zero Spring, precompiled JTE templates
  - [x] 1.2: Prerequisites: Java 21+, Maven 3.9+
  - [x] 1.3: Build in isolation: `mvn verify -pl starter-templates -am`
  - [x] 1.4: Run/Use: runs as library, no standalone process
  - [x] 1.5: Example: Java snippet calling `GenerationEngine.generate(config)` and asserting ZIP non-empty

- [x] Task 2: Create `starter-server/README.md`
  - [x] 2.1: Role section: Spring Boot REST API, delegates to starter-templates
  - [x] 2.2: Prerequisites: Java 21+, Maven 3.9+
  - [x] 2.3: Build in isolation: `mvn verify -pl starter-server -am`
  - [x] 2.4: Run locally: `mvn spring-boot:run -pl starter-server -am` (starts on port 8080)
  - [x] 2.5: Example: curl `POST /api/v1/generate` saving output to `my-project.zip`

- [x] Task 3: Create `starter-web/README.md`
  - [x] 3.1: Role section: Vue 3 SPA, Vite, Tailwind, served as static assets from starter-server
  - [x] 3.2: Prerequisites: Node.js 22+, npm 10+
  - [x] 3.3: Build in isolation: `npm ci && npm run build` from `starter-web/`
  - [x] 3.4: Run dev server: `npm run dev` — proxies `/api/**` to `http://localhost:8080`
  - [x] 3.5: Note: use `docker compose -f docker-compose.dev.yml up` for the backend

- [x] Task 4: Create `starter-mcp/README.md`
  - [x] 4.1: Role section: MCP npm package exposing `generate_project` tool for AI assistants
  - [x] 4.2: Prerequisites: Node.js 22+, npm 10+
  - [x] 4.3: Build in isolation: `npm ci && npm run build` from `starter-mcp/`
  - [x] 4.4: Run/Use: not a standalone process; registered in AI assistant MCP config
  - [x] 4.5: Example: complete Claude Desktop `claude_desktop_config.json` snippet

- [x] Task 5: Create `starter-archetypes/README.md`
  - [x] 5.1: Role section: GenerationClient interface + Maven archetype integration
  - [x] 5.2: Prerequisites: Java 21+, Maven 3.9+
  - [x] 5.3: Build in isolation: `mvn verify -pl starter-archetypes -am`
  - [x] 5.4: Note: references `GenerationClient` for archetype integration pattern

## Dev Notes

- No submodule READMEs exist yet; only `README.md` at project root exists
- Keep each README under ~80 lines — focused, not exhaustive; a contributor should get to "working" in under 5 minutes
- Submodule directory structure: `starter-templates/`, `starter-server/`, `starter-web/`, `starter-mcp/`, `starter-archetypes/`
- For Java submodules: the `-am` flag in `mvn verify -pl X -am` builds all dependencies of X automatically
- `starter-web` Vite dev server proxy config is in `starter-web/vite.config.ts` — proxy target is `http://localhost:8080`
- The root `README.md` already has good "Self-Hosting" and "Architecture" sections — cross-reference rather than duplicate
- Avoid documenting things that will rot quickly (specific dependency versions in body text — those belong in pom.xml/package.json)

## Dev Agent Record

### Implementation Plan

Created all 5 submodule READMEs following the story template. Each covers Role, Prerequisites, Build in isolation, Run/Use locally, and Example. Cross-referenced the root README rather than duplicating env var tables or architecture diagrams. Verified Vite proxy target (`http://localhost:8080`) from `vite.config.ts`.

### Debug Log

(none)

### Completion Notes

All 5 tasks complete. All 5 READMEs created, all under 80 lines. Ready for review.

## File List

- `starter-templates/README.md` — created
- `starter-server/README.md` — created
- `starter-web/README.md` — created
- `starter-mcp/README.md` — created
- `starter-archetypes/README.md` — created

## Change Log

- 2026-05-31: Story implemented by Dev Agent. All tasks complete, status → review.
