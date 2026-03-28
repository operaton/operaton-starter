# Story 1.1: Bootstrap Monorepo Structure

## Status
ready-for-dev

## Story

As a **developer contributing to operaton-starter**,
I want the monorepo to be fully bootstrapped with all modules wired and building green,
So that I can clone the repository and start working immediately without any manual setup.

## Acceptance Criteria

1. **Given** a developer clones the repository **When** they run `mvn verify` from the project root **Then** all 5 Maven modules build successfully with zero compilation errors and zero test failures

2. **Given** the monorepo structure **When** inspecting the root `pom.xml` **Then** it declares all 5 modules as children: `starter-server`, `starter-templates`, `starter-archetypes`, `starter-web`, `starter-mcp`

3. **Given** `starter-server` **When** inspected **Then** it is bootstrapped from Spring Initializr with Spring Boot 4.0.4, Java 21, Maven, and dependencies: `spring-boot-starter-web`, `spring-boot-starter-actuator`, `spring-boot-starter-validation`; the Operaton BOM 2.0.0 is added as an imported BOM; root package is `org.operaton.dev.starter.server`

4. **Given** `starter-templates` **When** inspected **Then** it is a plain Maven module with root package `org.operaton.dev.starter.templates`, zero Spring dependencies in its POM, and an ArchUnit test that fails the build if any class imports from `org.springframework.*`

5. **Given** a class importing from `org.springframework.*` is introduced into `starter-templates` **When** `mvn verify` is run **Then** the ArchUnit test fails the build with a clear violation message

6. **Given** `starter-archetypes` **When** inspected **Then** it is a plain Maven module with root package `org.operaton.dev.starter.archetypes`

7. **Given** `starter-web` **When** inspected **Then** it is scaffolded via `npm create vue@latest` with TypeScript, Vue Router, Vitest, ESLint+Prettier (no Pinia); its Maven POM uses `frontend-maven-plugin` with pinned Node.js v22 and npm 10 to run `npm ci && npm run build` during `mvn verify`

8. **Given** `starter-mcp` **When** inspected **Then** it is scaffolded via `npm init` with TypeScript and `@modelcontextprotocol/sdk@1.28.0`; its Maven POM uses `frontend-maven-plugin` identically to `starter-web`

9. **Given** the project root **When** inspected **Then** it contains: `renovate.json` (file present); `.editorconfig`; `.gitignore`; `README.md` (skeleton); `docker-compose.dev.yml` (skeleton)

## Tasks/Subtasks

- [ ] Task 1: Create root Maven parent POM with 5 modules declared
  - [ ] 1.1: Create root `pom.xml` with groupId `org.operaton.dev`, artifactId `operaton-starter`, packaging `pom`
  - [ ] 1.2: Declare 5 child modules in root POM: starter-server, starter-templates, starter-archetypes, starter-web, starter-mcp
  - [ ] 1.3: Add Spring Boot parent BOM and Operaton BOM 2.0.0 to dependency management
  - [ ] 1.4: Configure Java 21 compiler settings in root POM
  - [ ] 1.5: Add pluginManagement for common plugins (maven-compiler-plugin, frontend-maven-plugin)

- [ ] Task 2: Create starter-server module
  - [ ] 2.1: Create `starter-server/pom.xml` inheriting root POM with Spring Boot dependencies
  - [ ] 2.2: Create main Application class at `org.operaton.dev.starter.server.StarterServerApplication`
  - [ ] 2.3: Create application.properties with basic config
  - [ ] 2.4: Create placeholder test

- [ ] Task 3: Create starter-templates module
  - [ ] 3.1: Create `starter-templates/pom.xml` with zero Spring dependencies (ArchUnit dependency only for tests)
  - [ ] 3.2: Create package directory `org.operaton.dev.starter.templates`
  - [ ] 3.3: Create ArchUnit test that fails if any class imports from `org.springframework.*`
  - [ ] 3.4: Create placeholder class to satisfy Maven module

- [ ] Task 4: Create starter-archetypes module
  - [ ] 4.1: Create `starter-archetypes/pom.xml` inheriting root POM
  - [ ] 4.2: Create root package `org.operaton.dev.starter.archetypes`
  - [ ] 4.3: Create placeholder class

- [ ] Task 5: Create starter-web module (Vue.js frontend)
  - [ ] 5.1: Create `starter-web/pom.xml` using frontend-maven-plugin with Node.js v22
  - [ ] 5.2: Create Vue.js project scaffold with TypeScript, Vue Router, Vitest, ESLint+Prettier
  - [ ] 5.3: Configure npm scripts: build, test, lint

- [ ] Task 6: Create starter-mcp module (TypeScript MCP)
  - [ ] 6.1: Create `starter-mcp/pom.xml` using frontend-maven-plugin with Node.js v22
  - [ ] 6.2: Create npm package.json with TypeScript and @modelcontextprotocol/sdk@1.28.0
  - [ ] 6.3: Create TypeScript configuration and minimal MCP server stub

- [ ] Task 7: Create root project files
  - [ ] 7.1: Create `renovate.json` with basic Renovate configuration
  - [ ] 7.2: Create `.editorconfig` with standard settings
  - [ ] 7.3: Update `.gitignore` with Maven, Node.js, and IDE patterns
  - [ ] 7.4: Update `README.md` with skeleton project description
  - [ ] 7.5: Create `docker-compose.dev.yml` skeleton

## Dev Notes

- Spring Boot 4.0.4 is specified in the story (verify availability; use latest 3.x if 4.x not yet stable)
- Operaton BOM version: 2.0.0
- frontend-maven-plugin: pin Node.js v22, npm 10
- starter-templates must have ZERO Spring dependencies — enforced by ArchUnit
- All modules should build with `mvn verify` from root

## Dev Agent Record

### Implementation Plan

(to be filled during implementation)

### Debug Log

(to be filled during implementation)

### Completion Notes

(to be filled during implementation)

## File List

(to be filled during implementation)

## Change Log

(to be filled during implementation)
