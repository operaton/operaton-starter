# Arc42 Section 1: Introduction and Goals

## What Is operaton-starter?

Operaton Starter is a stateless, open-source project generator hosted at `start.operaton.org` that bootstraps Operaton-based projects — process applications, process archives, and future project types — as downloadable, ready-to-build, immediately runnable project archives. It is the first and only dedicated project initializer for the Operaton ecosystem, filling the gap that Spring Initializr fills for Spring Boot and code.quarkus.io fills for Quarkus.

Access is available through three first-class channels: a web UI at `start.operaton.org`, the `npx operaton-starter` CLI, and a single `curl` command — all backed by the same REST API (`POST /api/v1/generate`). An MCP module exposes the same generation API to AI-assisted development workflows.

## Goals

### Quality Goals (Priority Order)

| Priority | Quality Goal | Motivation |
|----------|-------------|------------|
| 1 | **Correctness** | Every generated project compiles, tests pass, and starts on first run — no exceptions |
| 2 | **Developer Experience** | Practitioner flow completes in under 30 seconds; zero friction for known use cases |
| 3 | **Channel Consistency** | Web UI, CLI, MCP, and REST API invoke the same engine — identical output guaranteed |
| 4 | **Availability** | 99.9% uptime for `start.operaton.org`; stateless design enables this without operational complexity |
| 5 | **Version Currency** | Always current Operaton release; updated within 24h of any Operaton stable release |
| 6 | **Self-Hostability** | Docker image runs with zero external dependencies; enterprise-ready out of the box |

### Functional Goals

- Generate ready-to-build Operaton project archives (MVP: 2 types × 3 build systems = 6 combinations)
- Identity-aware scaffolding — Group ID, Artifact ID, and project name propagate into BPMN process IDs, Java packages, and `spring.application.name`
- Metadata-driven UI and CLI — no hardcoded option lists in any channel
- Self-hostable Docker image configurable via environment variables only

## User Personas and Journeys

### Marcus — The Practitioner
Senior Operaton developer who knows exactly what he wants. Arrives at the form view, configures in 30 seconds, clicks the IntelliJ deep-link, and is running the engine without touching a config file. The form view is his interface.

### Elena — The Camunda 7 Migrator (Explorer)
Process architect evaluating migration from Camunda 7. The gallery surfaces a "Camunda 7 Migration" project type (Phase 2). The generated `MIGRATION.md` gives her a concrete, scoped migration plan within minutes.

### Thomas — The BPMN Newcomer (Explorer)
Spring Boot developer new to BPM tooling. Uses the gallery and inline contextual help to discover the right project type (Process Archive vs. Process Application) without consulting external documentation.

### Priya — The Platform Engineer
Integrates the REST API and MCP module into an internal Backstage developer portal. Self-hosts the Docker image with enterprise defaults via environment variables.

### Klaus — The Self-Hosted Admin
Runs the Docker image on-premises with `DEFAULT_GROUP_ID`, `MAVEN_REGISTRY`, and `OPERATON_VERSION` environment variables. Zero external startup dependencies; simple quarterly update cadence.

## Stakeholders

| Role | Expectation |
|------|------------|
| Operaton Community / Open Source Users | A reliable, zero-friction entry point for starting Operaton projects |
| Operaton Core Team | A maintained, spec-correct generator that tracks Operaton releases within 24 hours |
| Enterprise Operators (Klaus, Priya) | A Docker image that self-hosts cleanly with env-var configuration |
| AI Assistants (Claude, Copilot, Cursor) | An MCP tool that scaffolds Operaton projects mid-conversation |
| Community Contributors | Open-source generation templates that are forkable and auditable |
