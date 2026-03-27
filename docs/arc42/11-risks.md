# Arc42 Section 11: Risks and Technical Debt

## Architecture Coherence Validation

All technology choices have been validated as mutually compatible:
- Spring Boot 4.0.4 (Jakarta EE 11) is compatible with Operaton BOM 2.0.0
- JTE operates as a pure-Java library with no framework dependency
- Vue 3 + Vite + Tailwind is a well-established combination
- Scalar served via static HTML eliminates Spring Boot version coupling risk entirely

**Overall status:** READY FOR IMPLEMENTATION — High Confidence

---

## Identified Risks

### RISK-01: Operaton BOM Breaking Change

**Category:** External dependency
**Likelihood:** Low
**Impact:** High

**Description:** A new Operaton stable release contains a breaking API change that causes generated project templates to produce non-compilable code.

**Mitigation:**
- CI test matrix runs on every Dependabot/Renovate PR that bumps the Operaton BOM
- The 24-hour SLA for version updates is conditional on CI matrix passing — a breaking release pauses the SLA clock until templates are fixed
- Generated projects use the stable release (not SNAPSHOT) — breaking changes are documented in Operaton release notes

**Residual risk:** Templates may need manual updates if Operaton introduces a deprecated-then-removed API. Accepted.

---

### RISK-02: JTE Precompilation Learning Curve

**Category:** Technology adoption
**Likelihood:** Medium
**Impact:** Medium

**Description:** JTE is less widely known than FreeMarker or Thymeleaf. Contributors unfamiliar with JTE may struggle with the precompilation model, syntax, or debugging.

**Mitigation:**
- JTE spike story (Story 2.1) validates the full precompilation pipeline before any template is authored
- JTE template compilation errors surface at build time (not runtime) — fail fast
- JTE documentation is comprehensive; examples are straightforward for Java developers

**Residual risk:** Initial contributor onboarding may be slower than with a more familiar template engine. Accepted.

---

### RISK-03: Rate Limiting Ineffectiveness on Horizontal Scale

**Category:** Architecture
**Likelihood:** Low
**Impact:** Low

**Description:** Bucket4j in-memory rate limiting is per-instance. A client hitting different replicas gets a higher effective rate limit than `10 req/min`.

**Mitigation:**
- This is a known, accepted trade-off (ADR-10). The service is free and open-source — the rate limit is a courtesy limit, not a security boundary.
- Egregious abuse (DDoS-level) would be handled at the reverse proxy / CDN layer, not the application layer.

**Residual risk:** Accepted by design.

---

### RISK-04: OpenAPI Generator Spring Boot 4 Compatibility

**Category:** Tooling
**Likelihood:** Low
**Impact:** Medium

**Description:** `openapi-generator-maven-plugin` must set `useSpringBoot3=true` to produce Spring Framework 6/7-compatible server stubs. If this flag behaves differently for Spring Boot 4.0.x, generated stubs may not compile.

**Mitigation:**
- Configuration note documented: `<useSpringBoot3>true</useSpringBoot3>` in `starter-server` POM
- Story 1.2 (OpenAPI spec authoring) includes validation that generated server stubs compile and wire correctly
- `build-java` CI job fails immediately if stubs don't compile

**Residual risk:** Low — `useSpringBoot3=true` is the established flag for Spring Framework 6+; Spring Boot 4.0.x uses Spring Framework 7.

---

### RISK-05: Scalar CDN Dependency for API Docs

**Category:** External dependency
**Likelihood:** Very Low
**Impact:** Low

**Description:** The Scalar API docs page loads Scalar JS from CDN. If the CDN is unavailable, the docs page fails to render.

**Mitigation:**
- The API itself (`POST /api/v1/generate`, `GET /api/v1/metadata`) does not depend on the CDN
- Scalar CDN outage only affects the `/api/v1/docs` page, not generation functionality
- Alternative: bundle Scalar locally (low-effort mitigation if CDN reliability becomes a concern)

**Residual risk:** Accepted for MVP. Bundling Scalar locally is a straightforward Phase 2 mitigation.

---

### RISK-06: `org.operaton.dev` GroupId Not Claimed

**Category:** Release process
**Likelihood:** Low (known action item)
**Impact:** High (blocks first Maven Central publish)

**Description:** The `org.operaton.dev` groupId must be claimed at `central.sonatype.com` before the first Maven Central publish. If this is not done before the first release pipeline runs, the publish will fail.

**Mitigation:**
- Explicitly documented as a pre-publish prerequisite in architecture and release workflow
- Must be done before Story 6.1 (Docker image) or 5.1 (CLI npm) release runs

**Residual risk:** Known action item; accepted.

---

### RISK-07: OpenRewrite `migrate-from-camunda-recipe` Maintenance

**Category:** External dependency
**Likelihood:** Low
**Impact:** Medium (Phase 2 feature only)

**Description:** The Camunda 7 Migration project type (Phase 2) depends on `operaton/migrate-from-camunda-recipe`. If this OpenRewrite recipe becomes unmaintained, the migration project type would produce outdated migration guidance.

**Mitigation:**
- Phase 2 dependency — not in MVP scope
- Architecture explicitly notes: fork under Operaton org if upstream lapses
- The recipe is an Operaton-owned project — maintenance is the Operaton community's responsibility

**Residual risk:** Phase 2 risk only; accepted.

---

## Technical Debt Register

| Debt Item | Category | When to Address |
|-----------|----------|-----------------|
| Contract-check as warning (not hard block) | CI discipline | Phase 2 — promote to hard block once spec is stable |
| Interactive CLI mode not implemented | Feature | Phase 2 — MVP delivers pipe mode only |
| `EmbeddedGenerationClient` not implemented | Architecture | Phase 2 — offline `mvn archetype:generate` |
| Scalar served from CDN (not bundled) | Operational | Phase 2 — bundle locally if CDN reliability is a concern |
| Camunda 7 Migration project type | Feature | Phase 2 — requires `migrate-from-camunda-recipe` integration |
| Java 17 NFR correction | Documentation | NFR13 in PRD states Java 21 as floor; corrected to Java 17 in architecture — PRD should be updated |

## Resolved Gaps

| Gap | Resolution |
|-----|-----------|
| FR16 — Shareable config link encoding | Individual URL query params (`?type=...&build=...`). `useShareableLink.ts` with round-trip unit test. |
| FR32 — MCP configurable base URL | `OPERATON_STARTER_URL` env var; default `https://start.operaton.org` |
| FR20 — Inline contextual help on form | Story 4.3 AC explicitly includes ? help icons on all configuration options |
| openapi-generator Spring Boot 4 | `<useSpringBoot3>true</useSpringBoot3>` in `starter-server` POM |
| Dev environment for frontend devs | `docker-compose.dev.yml` includes backend service (Story 3.1) |
| IDE deep-link ZIP mechanism | Deep-links pass the API generate URL to the IDE; IDE fetches ZIP directly |
| IP rate limiting behind reverse proxy | `X-Forwarded-For` extraction with direct IP fallback |
| `starter-cli` module placement | 6th Maven module with `frontend-maven-plugin` (consistent with "6 modules" count) |
