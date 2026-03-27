# Arc42 Section 10: Quality Requirements

## Quality Tree

```
Quality
├── Correctness (Priority 1)
│   ├── Generated projects compile and tests pass: 100%
│   ├── Generated projects start on first run: 100%
│   └── CI matrix covers all 6 combinations (2 types × 3 build systems)
├── Performance
│   ├── POST /api/v1/generate ≤ 1 second
│   └── File tree preview update ≤ 200ms (client-side)
├── Availability
│   ├── 99.9% uptime (public instance)
│   └── Zero external dependencies at startup
├── Developer Experience
│   ├── Practitioner flow: form-to-ZIP ≤ 30 seconds
│   └── Explorer flow: gallery-to-ZIP without external docs
├── Accessibility
│   └── WCAG 2.1 AA — full keyboard navigation, screen reader support
├── Security
│   ├── HTTPS enforced
│   ├── No PII persistence
│   └── Rate limiting (10 req/min/IP)
├── Maintainability
│   ├── Structured JSON logs
│   ├── Env-var-only Docker configuration
│   └── Automated dependency updates (Dependabot/Renovate)
└── Visual Quality
    └── Match operaton.org design system; benchmark: start.spring.io
```

## Non-Functional Requirements

### Performance

| ID | Requirement | Measure | Notes |
|----|------------|---------|-------|
| NFR1 | `POST /api/v1/generate` response time | ≤ 1 second | JTE precompiled templates; no subprocess |
| NFR2 | File tree preview update | ≤ 200ms | Client-side only; pure function of metadata |
| NFR3 | `GET /api/v1/metadata` response time | ≤ 200ms | Served from in-memory MetadataProvider |
| NFR4 | UI-to-ZIP download time | ≤ 30 seconds (total user flow) | Includes form fill, generate, download |

### Availability & Reliability

| ID | Requirement | Measure | Notes |
|----|------------|---------|-------|
| NFR5 | Public instance uptime | 99.9% | Stateless; achievable without DB |
| NFR6 | Zero external dependencies at startup | 0 external calls on startup | Docker image starts air-gapped |
| NFR7 | Generated project compile rate | 100% | Hard guarantee; CI matrix enforces |
| NFR8 | Generated project CI pass rate | 100% | GitHub Actions skeleton passes on first push |

### Security

| ID | Requirement | Implementation |
|----|------------|---------------|
| NFR9 | HTTPS only | Reverse proxy enforces; HTTP redirects to HTTPS |
| NFR10 | No user data persistence | Stateless; no DB; no session store |
| NFR11 | Rate limiting | Bucket4j in-memory; 10 req/min/IP; 429 + `Retry-After` |
| NFR12 | No stack traces in responses | `GlobalExceptionHandler` strips all stack traces |

### Compatibility

| ID | Requirement | Value |
|----|------------|-------|
| NFR13 | Generated project Java floor | Java 17 (default); picker: 17, 21, 25 |
| NFR14 | Gradle version | 8.x (pinned wrapper in generated projects) |
| NFR15 | Node.js (CLI, MCP) | Active LTS at build time |
| NFR16 | Browser support | Latest 2 versions of major browsers |
| NFR17 | Server runtime Java | Java 25 (eclipse-temurin:25-jre-alpine) |

### Scalability

| ID | Requirement | Implementation |
|----|------------|---------------|
| NFR18 | Horizontal scaling | Share-nothing; stateless; no sticky sessions |
| NFR19 | Rate limiting model | Best-effort on horizontal scale (in-memory per instance) |

### Maintainability & Operations

| ID | Requirement | Implementation |
|----|------------|---------------|
| NFR20 | Visual consistency | Match operaton.org design system; design tokens extracted from Jekyll source |
| NFR21 | Structured logging | Logback + `logstash-logback-encoder`; JSON format |
| NFR22 | Observability | `/actuator/health` endpoint (health only; no metrics by default) |
| NFR23 | Docker configuration | Env-var only; no file-based config at runtime |
| NFR24 | Version update SLA | Operaton release → starter updated within 24h (conditional on CI matrix pass) |
| NFR25 | Dependency automation | Renovate/Dependabot PR + CI matrix = automated version update path |

### Accessibility

| ID | Requirement | Implementation |
|----|------------|---------------|
| NFR26 | WCAG 2.1 AA compliance | axe-core in CI (hard block on violations) |
| NFR27 | Full keyboard navigation | Tab order, focus management, keyboard-complete generation flow |
| NFR28 | Screen reader support | `aria-live="polite"` on preview; `role="alert"` on errors |

## Measurable Outcomes

| Outcome | Target | Measurement |
|---------|--------|-------------|
| REST API generation time | ≤ 1 second | CI performance test |
| UI-to-ZIP flow | ≤ 30 seconds | Manual acceptance gate |
| Generated project compile rate | 100% | CI test matrix (6 combinations) |
| Generated project CI pass rate | 100% | CI smoke test (mvn spring-boot:run → health 200 in 60s) |
| Version update lag | ≤ 24 hours | Time from Operaton release to merged Dependabot PR with passing CI |
| Availability | 99.9% | Uptime monitoring |
| WCAG 2.1 AA | 0 axe-core violations | CI `lint-web` job (hard block) |
