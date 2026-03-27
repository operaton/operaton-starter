# operaton-starter Documentation

Project documentation for [operaton-starter](https://github.com/operaton/operaton-starter) — the open-source project generator for the Operaton BPM ecosystem, hosted at [start.operaton.org](https://start.operaton.org).

## Contents

### [arc42/](arc42/) — Architecture Documentation

System architecture documented using the [arc42](https://arc42.org) template.

| Section | Description |
|---------|-------------|
| [01 — Introduction & Goals](arc42/01-introduction-and-goals.md) | Project overview, quality goals, personas, stakeholders |
| [02 — Constraints](arc42/02-constraints.md) | Technical and organizational constraints, naming conventions |
| [03 — Context](arc42/03-context.md) | System context diagram, external interfaces, channel matrix |
| [04 — Solution Strategy](arc42/04-solution-strategy.md) | Key technology decisions, implementation sequence, module bootstrapping |
| [05 — Building Block View](arc42/05-building-block-view.md) | Module responsibilities, package layout, complete project tree |
| [06 — Runtime View](arc42/06-runtime-view.md) | Generation flow, metadata/preview, rate limiting, CLI/MCP scenarios |
| [07 — Deployment View](arc42/07-deployment-view.md) | Docker image, CI/CD pipeline, self-hosted setup, local dev |
| [08 — Cross-Cutting Concepts](arc42/08-crosscutting-concepts.md) | Domain model ownership, error handling, logging, testing patterns |
| [09 — Architecture Decisions](arc42/09-architecture-decisions.md) | ADRs 01–10 (JTE, OpenAPI spec-first, Scalar, Tailwind, stateless design, …) |
| [10 — Quality Requirements](arc42/10-quality-requirements.md) | NFRs, quality tree, measurable outcomes |
| [11 — Risks](arc42/11-risks.md) | Risk register, technical debt, resolved architectural gaps |
| [12 — Glossary](arc42/12-glossary.md) | Domain terms, technology terms, personas, environment variables |

### [bmad/](bmad/) — Planning Artifacts

Artifacts produced during the BMAD planning workflow.

| Artifact | Description |
|----------|-------------|
| [Product Requirements Document](bmad/planning-artifacts/prd.md) | Full PRD — 44 FRs, 20 NFRs, user journeys, product scope, phased roadmap |
| [Architecture Decision Document](bmad/planning-artifacts/architecture.md) | Collaborative architecture document — all decisions, patterns, project tree |
| [Epics & Stories](bmad/planning-artifacts/epics.md) | 6 epics, 28 stories with Given/When/Then acceptance criteria |
| [Brainstorming Session](bmad/brainstorming/brainstorming-session-2026-03-25-1.md) | Initial ideation session that seeded the PRD |
