# Arc42 Section 4: Solution Strategy

## Key Technology Decisions

### Template Engine: JTE (Java Template Engine)

**Decision:** JTE with precompilation via `jte-maven-plugin`.

**Why:** JTE templates compile to Java classes at build time. At runtime, template rendering is a method call with no parsing overhead. This is the only viable approach that meets the ≤1s generation target without a subprocess. The alternative (Maven Archetype subprocess) is incompatible with the performance NFR.

**Consequences:** JTE spike required as first `starter-templates` story to validate the precompilation pipeline before any template is authored.

### OpenAPI Spec-First Discipline

**Decision:** `openapi.yaml` at project root is the single source of truth. All server stubs and channel clients are generated from it via `openapi-generator`.

**Why:** Prevents API drift between channels by construction. Server stubs, web client, MCP client, and CLI client are all generated — they cannot diverge.

**Consequences:** Spec must be frozen before any channel implementation begins. Post-freeze changes require regenerating all clients. CI enforces this as a PR status check.

### OpenAPI Documentation: Scalar via Static HTML + CDN

**Decision:** Static `openapi.yaml` at `/static/openapi.yaml`; Scalar rendered via static HTML at `/api/v1/docs` loading Scalar JS from CDN.

**Why:** Zero Spring Boot version coupling. No `scalar-spring-boot-starter` compatibility risk. Works identically on localhost and production.

**Consequences:** Scalar CDN dependency for docs page only (not for API itself). Spring Boot actuator provides health; Scalar provides docs.

### CSS/Styling: Tailwind CSS with operaton.org Design Tokens

**Decision:** Tailwind CSS with custom theme configuration mapping design tokens extracted from `github.com/operaton/operaton.org` (Jekyll source).

**Why:** Visual consistency with `operaton.org` without copy-pasting CSS. Tailwind config also exports tokens as CSS custom properties for non-utility custom CSS.

**Consequences:** Token extraction must happen before `starter-web` implementation begins. Static class names only (no dynamic Tailwind class construction).

### Docker Base Image: `eclipse-temurin:25-jre-alpine`

**Decision:** `eclipse-temurin:25-jre-alpine` for the production Docker image.

**Why:** Alpine minimizes image size. Eclipse Temurin is the community-maintained OpenJDK distribution used by `start.spring.io`. Java 25 is the runtime; generated projects default to Java 17.

**Consequences:** Layered JAR extraction (`COPY --from=builder`) for Docker layer cache efficiency.

### Generated Project Java Version: Default 17, Picker Offers 17/21/25

**Decision:** Java 17 as the default; picker offers 17, 21, 25. All three validated in CI matrix.

**Why:** Java 17 is the most widely deployed LTS. Java 25 is current LTS. The picker covers the realistic range without overwhelming users.

**Consequences:** CI test matrix must cover all three Java versions for generated projects.

## Implementation Sequence

The sequence is driven by the spec-first constraint — the API contract must exist before any channel can be implemented.

```
1. Author openapi.yaml + metadata schema     ← no code; spec freeze gates all channel work
2. Spike JTE precompilation                  ← validate zero-Spring + performance before authoring templates
3. Implement generation engine (starter-templates)
4. Wire engine into starter-server REST API  ← spec frozen after this step
5. Extract operaton.org design tokens → tailwind.config.js
6. Implement starter-web and starter-mcp in parallel (both consume frozen spec)
7. Implement starter-archetypes RestGenerationClient (MVP) last
```

## Module Bootstrapping

### `starter-server` — Spring Boot Application

Bootstrapped from Spring Initializr (`start.spring.io`):

```bash
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=4.0.4 \
  -d javaVersion=21 \
  -d groupId=org.operaton.dev \
  -d artifactId=starter-server \
  -d dependencies=web,actuator,validation \
  -o starter-server.zip
```

Operaton BOM 2.0.0 added manually as imported BOM. `useSpringBoot3=true` required in `openapi-generator-maven-plugin` for Spring 6/7-compatible server stubs.

### `starter-templates` — Pure Java Generation Engine

Plain Maven module (no generator). Zero Spring dependencies. JTE as template engine, precompiled at build time. Single public API: `GenerationEngine.generate(ProjectConfig) → byte[]`.

### `starter-archetypes` — GenerationClient Interface

Plain Maven module. Defines `GenerationClient` interface. MVP: `RestGenerationClient` (Java `HttpClient`). Phase 2: `EmbeddedGenerationClient` (direct call to `starter-templates`, no network).

### `starter-web` — Vue 3 SPA

Scaffolded with `npm create vue@latest`:
- TypeScript ✓, Vue Router ✓, Vitest ✓, ESLint+Prettier ✓
- Pinia: ✗ (form state is local; no global state manager needed)

Output served as static assets embedded in Spring Boot JAR (`starter-server/src/main/resources/static/`).

### `starter-mcp` — MCP npm Package

Manual setup with `@modelcontextprotocol/sdk` 1.28.0. Client code generated from frozen OpenAPI spec. Published as `operaton-starter-mcp` on npm.

### `starter-cli` — CLI npm Package

Manual setup. Published as `operaton-starter` npm package. Dual-mode: pipe to stdout (scriptable) / TTY interactive (Phase 2). Frontend-maven-plugin wraps it as a Maven module.

## Frontend-Maven-Plugin Hermetic Builds

`starter-web`, `starter-mcp`, and `starter-cli` are Maven modules using `com.github.eirslett:frontend-maven-plugin` to download a pinned Node.js/npm version and execute `npm ci && npm run build`. This ensures hermetic builds — no system Node.js dependency, identical Node version in CI and local builds.

## Data Architecture

- **No database** — all request state is ephemeral; cleared after each response
- **Rate limiting** — Bucket4j in-memory, 10 req/min/IP, best-effort (no Redis, no sticky sessions)
- **No caching layer** — JTE precompiled templates make generation fast enough (≤1s); no cache invalidation complexity
- **No authentication** — stateless; no user profiles, no sessions

## Deferred Decisions

| Decision | Status | Notes |
|----------|--------|-------|
| `EmbeddedGenerationClient` | Phase 2 | Offline `mvn archetype:generate` without network dependency |
| Scalar Spring Boot starter | Post-MVP | Pending Spring Boot 4 compatibility; static HTML+CDN preferred anyway |
| Contract-check hard merge block | Phase 2 | Currently warning-level; promote to hard block once spec is stable |
| Interactive CLI mode | Phase 2 | MVP delivers pipe/non-interactive mode only |
| Camunda 7 Migration project type | Phase 2 | OpenRewrite integration deferred |
