# Arc42 Section 6: Runtime View

## Scenario 1: Project Generation Request

```
Client → POST /api/v1/generate
  → GenerateController (validates DTO from target/generated-sources/)
  → GenerationService (maps DTO → ProjectConfig)
  → GenerationEngine.generate(ProjectConfig)
  → ZipGenerator (iterates TemplateManifest)
  → TemplateRenderer (JTE precompiled classes)
  → byte[] ZIP → 200 OK (application/zip)
```

**Key runtime properties:**
- No subprocess invocation. JTE template rendering is a Java method call on precompiled classes.
- DTO → `ProjectConfig` mapping happens in `GenerationService` — controllers see generated DTOs, engine sees domain model
- `@ControllerAdvice` in `GlobalExceptionHandler` intercepts all exceptions and translates to RFC 7807 Problem Details
- Target: ≤1 second end-to-end for `POST /api/v1/generate`

## Scenario 2: Metadata + Client-Side Preview

```
Client → GET /api/v1/metadata
  → MetadataController → MetadataService → MetadataProvider
  → MetadataResponse JSON (includes templateManifest per projectType)
  → Vue FileTreePreview: pure function of templateManifest + formState
  → no further server calls for preview updates
```

**Key runtime properties:**
- `templateManifest` is a flat list of `{ path, condition, templateId }` enabling client-side rendering
- Preview updates are pure client-side computation — zero server round-trips after initial metadata load
- Target: ≤200ms preview update after any input change

## Scenario 3: Rate Limit Enforcement

```
Client → POST /api/v1/generate
  → RateLimitFilter (Bucket4j in-memory)
    → if within limit: proceed to GenerateController
    → if exceeded: 429 Too Many Requests + Retry-After header
```

**Key runtime properties:**
- Rate limit: 10 requests/minute per IP
- IP extracted from `X-Forwarded-For` header (behind reverse proxy); falls back to direct connection IP
- In-memory: best-effort on horizontal scale; no Redis; no sticky sessions required
- `Retry-After` header value = seconds until next token available in bucket

## Scenario 4: Build-Time Spec Propagation

```
openapi.yaml (project root)
  → starter-server: openapi-generator-maven-plugin → target/generated-sources/openapi/dto/
  → starter-web:    openapi-generator (npm) → src/generated/
  → starter-mcp:    openapi-generator (npm) → src/generated/
  → starter-cli:    openapi-generator (npm) → src/generated/
```

This happens at build time (Maven `generate-sources` phase / npm build). At runtime, all channels use their generated client code — there is no dynamic spec loading.

## Scenario 5: CLI Pipe Mode

```
User → npx operaton-starter generate --type process-application --build maven ...
  → starter-cli index.ts (TTY detection → pipe mode)
  → generated API client → POST https://start.operaton.org/api/v1/generate
  → response body (byte[]) piped to stdout
  → user redirects: npx operaton-starter ... > project.zip
```

**Key runtime properties:**
- `OPERATON_STARTER_URL` env var overrides the base URL (for self-hosted or local development)
- Pipe mode: raw bytes to stdout; no progress indicators; exit code 0 on success, non-zero on error

## Scenario 6: MCP Tool Call

```
AI Assistant → MCP tool call: generate_project({ type, build, groupId, artifactId, ... })
  → starter-mcp generateProject.ts
  → generated API client → POST /api/v1/generate
  → ZIP bytes returned to AI assistant
```

**Key runtime properties:**
- `OPERATON_STARTER_URL` env var overrides base URL
- Tool schema mirrors OpenAPI spec request body — kept in sync by generator

## Scenario 7: Docker Health Check

```
Load Balancer → GET /actuator/health
  → Spring Boot Actuator → 200 OK { "status": "UP" }
```

**Key runtime properties:**
- Only `/actuator/health` exposed; no metrics endpoint by default
- Used by load balancer for instance routing decisions
- Returns `503 Service Unavailable` if application context is not healthy

## Error Handling Runtime Behaviour

### Java (`starter-server`)
- Domain exceptions thrown in service layer
- `GlobalExceptionHandler` (@ControllerAdvice) catches all exceptions
- Translates to RFC 7807 Problem Details: `application/problem+json`
- `ERROR` log with full stack trace in handler; client receives no stack trace
- No try/catch in controllers

### TypeScript (`starter-web`)
- `useApiError` composable wraps all API calls
- Exposes `{ error: Ref<ProblemDetail | null> }`
- All errors displayed via shared `<ErrorBanner>` component
- Network failures mapped to synthetic Problem Detail with `status: 0`

## API Response Formats

**Successful generation (200 OK):**
```
Content-Type: application/zip
Content-Disposition: attachment; filename="my-project.zip"
[binary ZIP data]
```

**Error response (RFC 7807):**
```json
{
  "type": "https://start.operaton.org/errors/rate-limit-exceeded",
  "title": "Rate Limit Exceeded",
  "status": 429,
  "detail": "10 requests per minute exceeded. Retry after 42 seconds.",
  "instance": "/api/v1/generate"
}
```

**HTTP status codes:**

| Situation | Status |
|-----------|--------|
| Successful generation | `200 OK` |
| Invalid request body | `400 Bad Request` |
| Rate limit exceeded | `429 Too Many Requests` + `Retry-After` |
| Server error | `500 Internal Server Error` |
