# Arc42 Section 8: Cross-Cutting Concepts

## Domain Model Ownership

`starter-templates` owns the shared domain model. All other modules import from it — no module defines its own parallel representation.

| Type | Package | Notes |
|------|---------|-------|
| `ProjectConfig` | `org.operaton.dev.starter.templates.model` | Java record; immutable |
| `ProjectType` | `org.operaton.dev.starter.templates.model` | Enum: `PROCESS_APPLICATION`, `PROCESS_ARCHIVE` |
| `BuildSystem` | `org.operaton.dev.starter.templates.model` | Enum: `MAVEN`, `GRADLE_GROOVY`, `GRADLE_KOTLIN` |
| `DeploymentTarget` | `org.operaton.dev.starter.templates.model` | Enum: `TOMCAT`, `STANDALONE_ENGINE` |

**Rule:** `starter-server` request DTOs are generated from `openapi.yaml` and mapped to `ProjectConfig` in `GenerationService`. They are not the domain model. Never redefine these types in any other module.

## Naming Patterns

### Java (all modules)

**Package root:** `org.operaton.dev.starter.*`

| Module | Root Package |
|--------|-------------|
| `starter-server` | `org.operaton.dev.starter.server` |
| `starter-templates` | `org.operaton.dev.starter.templates` |
| `starter-archetypes` | `org.operaton.dev.starter.archetypes` |

**Code conventions:**
- Classes: `PascalCase`
- Methods / variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Test classes: suffix `Test` → `GenerationEngineTest`
- No abbreviations in public API names: `generateProject`, not `genProj`

### API Endpoints

- Plural resource nouns: `POST /api/v1/generate`, `GET /api/v1/metadata`, `GET /api/v1/docs`
- Never: `/api/v1/generateProject`, `/api/v1/getMetadata`

### JSON Fields

- `camelCase` throughout (Jackson default)
- ✅ `groupId`, `artifactId`, `projectName`, `buildSystem`, `javaVersion`
- ❌ `group_id`, `artifact_id`, `GroupId`

### TypeScript (`starter-web`, `starter-mcp`, `starter-cli`)

| Item | Convention | Example |
|------|-----------|---------|
| Vue components | `PascalCase` filename | `ProjectGallery.vue` |
| Composables | `use` prefix | `useMetadata.ts` |
| Types / interfaces | `PascalCase` | `ProjectConfig`, `MetadataResponse` |
| Variables / functions | `camelCase` | `isLoading`, `fetchMetadata` |
| Generated files | In `src/generated/` — never hand-edited | — |

## Error Handling

### Java (`starter-server`)

- Domain exceptions thrown in service layer
- Single `@ControllerAdvice` (`GlobalExceptionHandler`) translates all to Problem Details
- No try/catch in controllers
- `ERROR` log with full stack trace in handler; client receives no stack trace

**Error response format — RFC 7807 Problem Details:**
```json
{
  "type": "https://start.operaton.org/errors/rate-limit-exceeded",
  "title": "Rate Limit Exceeded",
  "status": 429,
  "detail": "10 requests per minute exceeded. Retry after 42 seconds.",
  "instance": "/api/v1/generate"
}
```

**Never:** Response wrapper `{ "data": {...}, "status": "ok" }`. Direct responses + Problem Details only.

### TypeScript (`starter-web`)

- `useApiError` composable wraps all API calls; exposes `{ error: Ref<ProblemDetail | null> }`
- Errors displayed via shared `<ErrorBanner>` component — no inline error handling in individual components
- Network failures mapped to synthetic Problem Detail with `status: 0`

## Vue Composable Contract

All API composables in `starter-web` expose the same shape:

```typescript
const { data, isLoading, error } = useMetadata()
const { data, isLoading, error } = useGenerate()
// data: Ref<T | null>, isLoading: Ref<boolean>, error: Ref<ProblemDetail | null>
```

Loading states: per-operation `ref<boolean>` — `isLoadingMetadata`, `isGenerating`. Set `true` before fetch, `false` in `finally`. No global loading state.

## Validation

- **Java:** Bean Validation (`@Valid`) on request DTOs; errors return `400` Problem Detail automatically
- **TypeScript:** Client-side form validation mirrors OpenAPI spec field constraints
- `deploymentTarget` is optional in the spec; validated server-side (required for `PROCESS_ARCHIVE`, ignored for `PROCESS_APPLICATION`)

## Logging (`starter-server`)

- Structured JSON only (Logback + `logstash-logback-encoder`)
- `ERROR` — exceptions (with stack trace)
- `WARN` — rate limit hits
- `INFO` — generation requests (with `projectType`, `buildSystem`, `javaVersion`)
- `DEBUG` — template rendering
- **Never log IP addresses** — IP used only in rate limiter, never in log body

## Testing Patterns

### Generation Output Contract Test (`starter-templates`)

Every project type × build system combination has a `@ParameterizedTest` that:
1. Calls `GenerationEngine.generate(config)`
2. Extracts the ZIP in-memory
3. Asserts specific files exist at specific paths
4. Asserts identity propagation in each relevant file:
   - `groupId` / `artifactId` / `projectName` appear in `pom.xml` or `build.gradle`
   - Package path matches `groupId.artifactId` in all Java source files
   - BPMN process ID contains `artifactId`
   - `spring.application.name` in `application.properties` matches `projectName`

### Zero-Spring Enforcement (`starter-templates`)

```java
@Test
void templateModuleHasNoSpringDependencies() {
    noClasses()
        .that().resideInAPackage("org.operaton.dev.starter.templates..")
        .should().dependOnClassesThat()
        .resideInAPackage("org.springframework..")
        .check(importedClasses);
}
```

### Shareable Link Round-Trip Test (`starter-web`)

`useShareableLink.ts` must have a Vitest round-trip test:
```
serialize(formState) → query string → parse(queryString) → assert equals formState
```
Required for every new config field. Prevents silent breakage of shareable links.

## Security Concepts

| Concept | Implementation |
|---------|---------------|
| HTTPS | Enforced at reverse proxy; HTTP redirects to HTTPS |
| Rate limiting | Bucket4j in-memory; 10 req/min/IP; `X-Forwarded-For` extraction |
| No PII | Only transient IP data for rate limit window; never logged; never persisted |
| CORS | Configured for `start.operaton.org` + `localhost`; self-hosted adds via `CORS_ALLOWED_ORIGINS` |
| No authentication | Stateless by design; no user profiles, no sessions |
| No stack traces in responses | `GlobalExceptionHandler` strips stack traces from all error responses |

## Generated Code Boundary

**Rule:** `src/generated/` in any module is owned by the OpenAPI generator. No human or agent edits these files. Spec changes require re-running `openapi-generator`.

The generated sources are output to `target/generated-sources/openapi/` for Java (never to `src/`; gitignored automatically) and to `src/generated/` for TypeScript (tracked in git; owned by generator).

## Anti-Patterns (Forbidden)

- ❌ Response wrapper `{ data: ..., error: ... }` — use direct responses + Problem Details
- ❌ Calling `mvn archetype:generate` or any Maven subprocess at runtime
- ❌ Adding Spring dependencies to `starter-templates`
- ❌ Hardcoding project types, build systems, or Java version options in any channel
- ❌ Hand-editing `src/generated/` files in any module
- ❌ Exposing stack traces in API error responses
- ❌ Defining `ProjectConfig`, `BuildSystem`, `ProjectType`, or `DeploymentTarget` outside `starter-templates`
- ❌ Dynamic Tailwind class construction in `starter-web` (use static class names only)
