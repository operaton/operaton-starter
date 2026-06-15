# Examples Repository Format

## Overview

The operaton-starter **Examples Gallery** displays curated, runnable Operaton examples sourced from public GitHub repositories. This document is the **single source of truth** for the `.operaton-starter.yml` manifest format, enabling example authors to publish their work without trial-and-error.

### Rationale

The manifest-based approach decouples example discovery and metadata from the starter source code:

- **Distributed authorship**: Any GitHub repository can contribute examples via a `.operaton-starter.yml` manifest at its root
- **Flexible metadata**: Examples describe themselves with rich fields (buildSystem, tags, integrations, complexity)
- **Responsive gallery**: Operators can register new example sources (via environment variable or PR) without rebuilding the starter
- **Forward compatibility**: Unknown manifest fields are silently ignored, so new examples work with older starter versions
- **Pinned deployments**: Each manifest source is resolved to a commit SHA, so the gallery view and downloaded archives stay synchronized even as repositories advance

## Repository Structure

Each example source repository must have:

```
example-repo/
├── .operaton-starter.yml         # Manifest (required, at repo root)
├── examples/
│   ├── leave-request-spring-boot/
│   │   ├── pom.xml
│   │   ├── src/
│   │   └── README.md
│   ├── order-fulfillment-quarkus/
│   │   ├── build.gradle
│   │   ├── src/
│   │   └── README.md
│   └── incident-escalation-plain-java/
│       ├── pom.xml
│       ├── src/
│       └── README.md
└── README.md
```

**Key rules:**

- Manifest is **mandatory** at the repository root
- Each `examples[].path:` is relative to the repo root and must exist
- No `..` path traversal; no leading `/`; no null bytes
- Unknown manifest fields (at any nesting level) are silently ignored
- `apiVersion` is major-version gated: only `operaton-starter/v1*` manifests are accepted

## Full `.operaton-starter.yml` Schema

### Top-level

```yaml
apiVersion: operaton-starter/v1    # required
repository:                        # optional
  name: "Operaton Examples"        # optional, string
  description: "..."               # optional, string
  maintainer:                      # optional
    name: "..."                    # optional, string
    url: "..."                     # optional, URL
examples: [...]                    # required, list
```

### Example Entry

| Field | Required | Type | Notes |
|-------|----------|------|-------|
| `id` | yes | slug | Unique within the repo, `[a-z0-9-]+`, e.g., `leave-request-spring-boot` |
| `title` | yes | string | Display name, e.g., `"Leave Request (Spring Boot)"` |
| `icon` | no | emoji or path | Single emoji char (e.g., `📝`), or repo-relative path to SVG/PNG ≤ 64×64 |
| `path` | yes | path | Relative to repo root; no `..`, no leading `/`, e.g., `examples/leave-request-spring-boot` |
| `shortDescription` | yes | string ≤ 200 | One-line teaser for cards and list views |
| `longDescription` | no | markdown | Multi-paragraph description with bullet points, code blocks, etc. |
| `buildSystem` | no | enum | `maven` or `gradle` |
| `runtime` | no | enum | `spring-boot`, `quarkus`, `plain-java`, or `other` |
| `operatonVersion` | no | string | e.g., `"2.1.1"` or `"1.0.0-beta-5"` |
| `javaVersion` | no | string | e.g., `"21"` or `"17+"` |
| `complexity` | no | enum | `beginner`, `intermediate`, or `advanced` |
| `tags` | no | list of `{label, category}` | Facets for filtering: see section below |
| `integrations` | no | list of strings | Free-form: e.g., `[rest, dmn, kafka, external-task]` |
| `bpmnConcepts` | no | list of strings | Covered patterns: e.g., `[user-task, exclusive-gateway, compensation]` |
| `requires` | no | string | Free-form prerequisites, e.g., `"Java 21+, Docker (for Postgres)"` |
| `authors` | no | list of `{name, url?}` | Author names and GitHub/website links |
| `license` | no | SPDX ID | e.g., `"Apache-2.0"` |
| `documentationUrl` | no | URL | Link to example README or docs site |
| `demoVideoUrl` | no | URL | Link to demo/tutorial video |
| `screenshots` | no | list of paths | Repo-relative image paths for detail view |
| `lastUpdated` | no | ISO date | e.g., `"2026-06-10"`; falls back to GitHub commit date if omitted |

### Tags: Faceted Search

Tags enable filtering in the gallery. Each tag has a `label` (display string) and a `category`:

| Category | Examples | Purpose |
|----------|----------|---------|
| `runtime` | Spring Boot, Quarkus, Plain Java | Runtime environment filter |
| `buildSystem` | Maven, Gradle | Build tool filter |
| `complexity` | Beginner, Intermediate, Advanced | Skill level indicator |
| `concept` | Approval, DMN, REST, User Task, Compensation | Process pattern or domain concept |
| `integration` | Kafka, REST, Database, External Task | External system integration |

**Example tag list:**

```yaml
tags:
  - { label: "Approval", category: "concept" }
  - { label: "DMN", category: "concept" }
  - { label: "REST", category: "integration" }
  - { label: "Spring Boot", category: "runtime" }
  - { label: "Maven", category: "buildSystem" }
  - { label: "Beginner", category: "complexity" }
```

## Annotated Complete Example

```yaml
apiVersion: operaton-starter/v1

repository:
  name: "Operaton Examples"
  description: "Curated, runnable examples illustrating common Operaton patterns and integrations."
  maintainer:
    name: "Karsten Thoms"
    url: "https://github.com/kthoms"

examples:
  # Spring Boot + Maven: Simple approval flow with DMN policy
  - id: leave-request-spring-boot
    title: "Leave Request (Spring Boot + Maven)"
    icon: "📝"
    path: examples/leave-request-spring-boot
    shortDescription: >
      Classic leave-request approval flow with a REST API, a user task, and a DMN-based vacation policy.
    longDescription: |
      A complete, runnable Spring Boot example showing how to model a leave-request
      approval process in Operaton. Demonstrates:

      - Submitting a request via REST
      - Routing to a manager user task
      - Evaluating a DMN policy for auto-approval thresholds
      - Sending a notification on completion

      Includes integration tests and a `docker-compose.yml` for a Postgres backend.
    buildSystem: maven
    runtime: spring-boot
    operatonVersion: "2.1.1"
    javaVersion: "21"
    complexity: beginner
    tags:
      - { label: "Approval", category: "concept" }
      - { label: "DMN", category: "concept" }
      - { label: "REST", category: "integration" }
    integrations: [rest, dmn, postgres]
    bpmnConcepts: [user-task, exclusive-gateway, business-rule-task, service-task]
    requires: "Java 21+, Docker (for Postgres)"
    authors:
      - { name: "Karsten Thoms", url: "https://github.com/kthoms" }
    license: "Apache-2.0"
    documentationUrl: "https://github.com/kthoms/operaton-examples/blob/main/examples/leave-request-spring-boot/README.md"
    screenshots:
      - examples/leave-request-spring-boot/docs/process.png
    lastUpdated: "2026-06-10"

  # Quarkus + Gradle: Event-driven with external task workers
  - id: order-fulfillment-quarkus
    title: "Order Fulfillment (Quarkus + Gradle)"
    icon: "📦"
    path: examples/order-fulfillment-quarkus
    shortDescription: >
      End-to-end order fulfillment process on Quarkus with Kafka events and external task workers.
    longDescription: |
      A Quarkus-based example modeling order intake, payment, and shipping.

      - Kafka inbound trigger for new orders
      - External task pattern for payment and shipment services
      - Compensation on payment failure
    buildSystem: gradle
    runtime: quarkus
    operatonVersion: "2.1.1"
    javaVersion: "21"
    complexity: intermediate
    tags:
      - { label: "Order Mgmt", category: "concept" }
      - { label: "Kafka", category: "integration" }
      - { label: "External Task", category: "concept" }
    integrations: [kafka, rest, external-task]
    bpmnConcepts: [message-start-event, parallel-gateway, compensation, external-task]
    requires: "Java 21+, Docker (Kafka, Postgres)"
    authors:
      - { name: "Karsten Thoms", url: "https://github.com/kthoms" }
    license: "Apache-2.0"
    documentationUrl: "https://github.com/kthoms/operaton-examples/blob/main/examples/order-fulfillment-quarkus/README.md"
    lastUpdated: "2026-06-12"

  # Plain Java embedded: Minimal footprint with timers
  - id: incident-escalation-plain-java
    title: "Incident Escalation (Plain Java embedded)"
    icon: "🚨"
    path: examples/incident-escalation-plain-java
    shortDescription: >
      Minimal example embedding the Operaton engine in a plain Java app, no framework, with timer escalation.
    buildSystem: maven
    runtime: plain-java
    operatonVersion: "2.1.1"
    javaVersion: "17"
    complexity: advanced
    tags:
      - { label: "Embedded", category: "runtime" }
      - { label: "Timer", category: "concept" }
    integrations: []
    bpmnConcepts: [timer-boundary-event, escalation, sub-process]
    requires: "Java 17+"
    authors:
      - { name: "Karsten Thoms", url: "https://github.com/kthoms" }
    license: "Apache-2.0"
    lastUpdated: "2026-06-08"
```

## Forward Compatibility Contract

### Unknown Fields Are Silently Ignored

The parser accepts any unknown fields at any nesting level and ignores them gracefully. This allows example authors to:

- Add custom metadata fields for their own tooling
- Future-proof examples against starter schema extensions

```yaml
apiVersion: operaton-starter/v1
examples:
  - id: my-example
    path: examples/my-example
    shortDescription: "Example"
    customField: "This is silently ignored by the starter"
    myCompanyMetadata:
      someKey: value
```

**Result:** The example parses successfully; custom fields are not exposed in the gallery but do not break validation.

### `apiVersion` Major-Version Gating

The manifest `apiVersion` must start with `operaton-starter/v1`. Manifests with `v2` or higher are rejected at parse time.

| apiVersion | Status |
|------------|--------|
| `operaton-starter/v1` | Accepted |
| `operaton-starter/v1.2.3` | Accepted (v1 prefix) |
| `operaton-starter/v2` | Rejected |
| `operaton-starter/v0.5` | Rejected |
| (missing) | Rejected |

This ensures **backward compatibility**: old starter versions safely ignore newer manifests, and new starter versions can introduce breaking changes in `v2` without ambiguity.

## Registration: Adding Your Example Repository

### Option 1: Environment Variable (Self-Hosted)

When running operaton-starter in Docker or standalone mode, set the `STARTER_EXAMPLES_REPOSITORIES` environment variable to a comma-separated list of GitHub repository tokens:

```bash
docker run -p 8080:8080 \
  -e STARTER_EXAMPLES_REPOSITORIES="kthoms/operaton-examples,myorg/my-examples" \
  operaton/operaton-starter:latest
```

The starter will fetch `.operaton-starter.yml` from each repository's default branch at startup (and on manual `/api/v1/examples/refresh` POST).

### Option 2: Pull Request (Upstream starter-starter)

Contribute your repository to the default configuration by opening a PR to the operaton-starter repository:

1. Edit [`starter-server/src/main/resources/application.properties`](../starter-server/src/main/resources/application.properties)
2. Extend `starter.examples.repositories` with your repository token, e.g.:
   ```
   starter.examples.repositories=${STARTER_EXAMPLES_REPOSITORIES:kthoms/operaton-examples,myorg/my-examples}
   ```
3. Open a PR and describe your examples in the commit message
4. Once merged and released, your examples appear in the public `start.operaton.org` gallery

### Example Registration Checklist

Before registering your repository, verify:

- [ ] `.operaton-starter.yml` exists at the repo root
- [ ] `apiVersion: operaton-starter/v1` is present
- [ ] All `examples[].path` entries reference existing directories
- [ ] At least 3 examples cover the runtime matrix (Spring Boot, Quarkus, plain Java)
- [ ] Each example has a long description, emoji icon, and tags spanning multiple categories
- [ ] All URLs in `authors[].url`, `documentationUrl`, `demoVideoUrl` are live
- [ ] Examples build and run without errors
- [ ] License is declared (Apache-2.0 recommended for consistency)

## Testing Your Manifest

The starter includes a **smoke test** that validates example manifests against the committed fixture at build time:

```bash
./mvnw test -Dtest=ExampleManifestParserSmokeTest
```

This ensures your examples parse without rejection. For local testing before committing:

```bash
# Parse your manifest locally
java -cp starter-server/target/classes:... \
  org.operaton.dev.starter.server.examples.ExampleManifestParser \
  .operaton-starter.yml
```

Or use the **refresh endpoint** in a running starter instance:

```bash
curl -X POST http://localhost:8080/api/v1/examples/refresh \
  -H "Content-Type: application/json"
```

The response includes per-source success/failure details and example counts.

## References

- **Architecture Decisions**: [`docs/arc42/decisions.md`](../arc42/decisions.md) (Decisions A4, A12, A13)
- **API Schema**: Auto-generated OpenAPI documentation at `/v3/api-docs` (see `Example` model)
- **Seed Repository**: [`kthoms/operaton-examples`](https://github.com/kthoms/operaton-examples) with 3+ examples
