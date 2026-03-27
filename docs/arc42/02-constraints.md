# Arc42 Section 2: Constraints

## Technical Constraints

### Generation Engine

| Constraint | Rationale |
|-----------|-----------|
| `mvn archetype:generate` MUST NOT be called at runtime | Maven subprocess startup overhead violates NFR1 (≤1s generation). Maven Archetype format is used for template authoring only; the runtime engine is a pure-Java in-process library. |
| `starter-templates` must have zero Spring dependencies | Enables fast CI matrix and direct embedding in future `EmbeddedGenerationClient`. Enforced by ArchUnit test (`ZeroSpringDependencyTest`). |
| JTE templates precompiled at build time | No runtime template parsing. Template classes shipped in JAR. Zero-overhead rendering in production. |

### API & Spec Discipline

| Constraint | Rationale |
|-----------|-----------|
| OpenAPI spec frozen before client generation | `openapi.yaml` is the source of truth. Any post-freeze change requires regenerating all clients (web, MCP, CLI). CI enforces this as a PR status check. |
| No hardcoded option lists in any channel | All project types, build systems, and Java versions come from `GET /api/v1/metadata`. Changing options requires no code changes in channels. |
| Generated files in `src/generated/` are immutable | Only the OpenAPI generator may modify these. Hand-edits are overwritten on next generation run. |

### Infrastructure & Operations

| Constraint | Rationale |
|-----------|-----------|
| Stateless design — no database, no sessions | Enables horizontal scaling without sticky sessions. Simplifies operations. No PII persistence risk. |
| Rate limiting: Bucket4j in-memory, best-effort | No Redis or external store. Best-effort on horizontal scale — consistent with stateless constraint. |
| All runtime configuration via environment variables | Docker image runs with zero file-based config. Enterprise operators configure via `docker run -e`. |
| Docker base image: `eclipse-temurin:25-jre-alpine` | Fixed at architecture time. Runtime JRE is Java 25; generated projects default to Java 17. |
| HTTPS only | Enforced at reverse proxy / load balancer layer. Application serves HTTP; proxy handles TLS termination. |

### Dependency Constraints

| Constraint | Rationale |
|-----------|-----------|
| Operaton BOM: always current stable release | No version picker for public instance. Updated within 24h of Operaton stable release via Dependabot/Renovate PR + CI matrix pass. |
| `org.operaton.dev` groupId | Must be claimed at `central.sonatype.com` before first Maven Central publish. Verify namespace ownership before first release pipeline run. |
| OpenRewrite (`operaton/migrate-from-camunda-recipe`) | Phase 2 dependency. Tracked explicitly. Fork under Operaton org if upstream lapses. |

## Organizational Constraints

| Constraint | Impact |
|-----------|--------|
| Solo developer resource profile | Architecture prioritizes simplicity and automation over multi-team optimization. No distributed systems unless unavoidable. |
| Open-source project | Generation templates are forkable. No proprietary dependencies. All generated projects must be buildable without authentication. |
| Operaton release cadence | CI matrix must pass against new Operaton BOM within 24h. Automated dependency PRs are the mechanism. |
| Design system source: `github.com/operaton/operaton.org` (Jekyll) | CSS design tokens extracted from Jekyll source before `starter-web` implementation begins. Tailwind config maps tokens. |

## Conventions

| Convention | Scope |
|-----------|-------|
| Java packages: `org.operaton.dev.starter.*` | All Java modules |
| JSON field naming: `camelCase` throughout | All API request/response bodies |
| Error responses: RFC 7807 Problem Details (`application/problem+json`) | All error HTTP responses from `starter-server` |
| API versioning: base path `/api/v1/` | Versioned from day one |
| ISO 8601 date/time strings | All date/time fields in API |
| Structured JSON logging (Logback + logstash-logback-encoder) | `starter-server` logs only |
| Vue composables: `{ data, isLoading, error }` contract | All API composables in `starter-web` |
