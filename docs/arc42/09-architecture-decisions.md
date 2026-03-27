# Arc42 Section 9: Architecture Decisions

## ADR-01: Unified Generation Engine — No Per-Channel Logic

**Status:** Accepted

**Context:** Multiple channels (web UI, REST API, CLI, MCP, `mvn archetype:generate`) all generate the same output. Without a shared engine, each channel could evolve independently, leading to divergence in generated project content.

**Decision:** A single `GenerationEngine.generate(ProjectConfig) → byte[]` in `starter-templates` is the only code that produces project archives. All channels are thin wrappers that call this engine (directly or via REST API). No channel contains template logic.

**Consequences:**
- Channel consistency is enforced by construction, not by convention
- CI test matrix tests the engine output directly — passing matrix proves all channels produce correct output
- Adding a new project type requires only new JTE templates in `starter-templates` and a metadata entry; no channel changes

---

## ADR-02: Maven Archetype Format as Authoring Standard, Not Runtime Execution

**Status:** Accepted

**Context:** Maven Archetype is the standard format for Java project templates. The obvious approach is to call `mvn archetype:generate` at request time. However, Maven subprocess startup time (~2–5 seconds) violates NFR1 (≤1s generation).

**Decision:** Maven Archetype format is used only for template authoring and as the conceptual model. The runtime engine is a pure-Java in-process library (`starter-templates`) that reads JTE templates directly without Maven.

**Consequences:**
- `mvn archetype:generate` is not available at MVP (the `starter-archetypes` module's `RestGenerationClient` delegates to the REST API)
- Phase 2 `EmbeddedGenerationClient` enables true offline `mvn archetype:generate` by calling `starter-templates` directly

---

## ADR-03: JTE as Template Engine (Precompiled, Zero Runtime Parsing)

**Status:** Accepted

**Context:** The generation engine needs a template engine. Options considered: FreeMarker, Thymeleaf, Velocity, Mustache, JTE. The constraint is ≤1s generation and zero Spring dependency.

**Decision:** JTE (Java Template Engine) with precompilation via `jte-maven-plugin`. Templates compile to Java classes at build time. Template rendering at runtime is a method call — no parsing overhead.

**Consequences:**
- A JTE spike is the first `starter-templates` story — validates precompilation pipeline before any template is authored
- JTE templates live in `src/main/jte/` and are compiled during the Maven `generate-sources` phase
- Adding a new template requires a Maven rebuild (acceptable — this is a CI concern, not a runtime concern)
- JTE templates are type-safe — template parameters are checked at compile time

---

## ADR-04: Spec-First OpenAPI Discipline with openapi-generator

**Status:** Accepted

**Context:** Multiple channels consume the same API. Without a single source of truth, server implementation and client expectations can drift silently.

**Decision:** `openapi.yaml` at project root is the single source of truth. Server stubs (`starter-server`) and all channel clients (`starter-web`, `starter-mcp`, `starter-cli`) are generated via `openapi-generator`. The spec is frozen before any channel implementation begins.

**Consequences:**
- `openapi-generator-maven-plugin` in `starter-server` uses `useSpringBoot3=true` for Spring Framework 6/7 compatibility
- Post-freeze spec changes require regenerating all clients — CI `contract-check` job detects drift
- `src/generated/` directories are owned by the generator; hand-edits are forbidden
- All JSON field names in requests/responses are defined in the spec — no implicit Jackson conventions

---

## ADR-05: Scalar API Docs via Static HTML + CDN (No Spring Boot Coupling)

**Status:** Accepted

**Context:** API documentation needs to be served at `/api/v1/docs`. Options: Springdoc OpenAPI (Swagger UI), Scalar Spring Boot starter, static HTML page loading Scalar from CDN.

**Decision:** Static `openapi.yaml` served from `src/main/resources/static/`; Scalar rendered via a static HTML page at `/api/v1/docs` that loads Scalar JS from CDN.

**Consequences:**
- Zero Spring Boot version coupling — works identically on Spring Boot 4.0.x and any future version
- No dependency on `scalar-spring-boot-starter` compatibility matrix
- Scalar CDN dependency for the docs page only (not for the API itself)
- Operaton.org brand colors applied via Scalar configuration in the static HTML

---

## ADR-06: Stateless Design — No Database, No Sessions

**Status:** Accepted

**Context:** The service generates project archives from request parameters. No user state needs to persist between requests.

**Decision:** No database, no session store, no Redis. All request state is ephemeral — processed and discarded within the HTTP request lifecycle.

**Consequences:**
- Horizontal scaling without sticky sessions — any replica handles any request
- 99.9% availability achievable for a stateless service without operational complexity
- Rate limiting is best-effort (Bucket4j in-memory per instance; no cross-instance coordination)
- No PII persistence risk — only transient IP data held for rate limit window

---

## ADR-07: GenerationClient Strategy Interface (MVP: REST → Phase 2: Embedded)

**Status:** Accepted

**Context:** `mvn archetype:generate` integration requires a `GenerationClient`. At MVP, the server must be reachable over HTTP. In Phase 2, offline generation is desirable.

**Decision:** `starter-archetypes` defines a `GenerationClient` interface. MVP: `RestGenerationClient` (Java `HttpClient`, delegates to `POST /api/v1/generate`). Phase 2: `EmbeddedGenerationClient` (calls `starter-templates` directly, no network).

**Consequences:**
- `starter-archetypes` depends on `starter-server` being deployed at MVP
- Phase 2 enables fully offline project generation without network
- The strategy pattern prevents engine logic duplication between REST and embedded paths

---

## ADR-08: Metadata Schema as First-Class Contract

**Status:** Accepted

**Context:** All channels (web UI, CLI, MCP) need to present the same list of project types, build systems, and options. Hardcoding these lists in each channel creates maintenance burden and divergence risk.

**Decision:** `GET /api/v1/metadata` is the projection contract between the engine and all consumers. The schema is defined before any channel implementation begins. `templateManifest` within each project type descriptor enables client-side file tree preview without server round-trips.

**Consequences:**
- Adding a new project type requires only a new metadata entry and JTE templates — no channel code changes
- `templateManifest` is an architectural artifact (not a convenience feature) — its schema is part of the API contract
- All channels are passively in sync with the engine's capabilities
- `personaHint` in each project type enables gallery cards to position project types for different developer personas

---

## ADR-09: Individual URL Query Params for Shareable Config Links

**Status:** Accepted

**Context:** FR16 requires shareable configuration links. Options: base64-encoded JSON blob, individual URL query params.

**Decision:** Individual URL query params — `?type=process-application&build=maven&groupId=com.example&artifactId=my-project&...`. `useShareableLink.ts` serializes/deserializes form state.

**Consequences:**
- Human-readable and debuggable — URL tells you exactly what configuration it encodes
- No decoder needed to inspect a link
- Round-trip unit test required in `useShareableLink.ts`
- Any new config field must update `useShareableLink.ts` and its round-trip test

---

## ADR-10: Best-Effort In-Memory Rate Limiting (Bucket4j, No Redis)

**Status:** Accepted

**Context:** The service must enforce rate limiting (10 req/min/IP) to prevent abuse. Options: Redis-backed distributed rate limiting, in-memory per-instance.

**Decision:** Bucket4j in-memory rate limiting per IP per instance. IP extracted from `X-Forwarded-For` header (reverse proxy) with direct connection fallback.

**Consequences:**
- No Redis dependency — preserves stateless constraint and operational simplicity
- Best-effort on horizontal scale — a client hitting different replicas gets a higher effective rate limit
- Acceptable for a free, open-source tool — not a security boundary, just a courtesy limit
- Self-hosted single-instance deployments get exact rate limiting
- `Retry-After` header indicates seconds until next request is allowed
