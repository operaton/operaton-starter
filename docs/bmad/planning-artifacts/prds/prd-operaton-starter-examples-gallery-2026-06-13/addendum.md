# Addendum — Examples Gallery PRD

## Full `.operaton-starter.yml` schema

```yaml
apiVersion: operaton-starter/v1   # required; major-version gated

repository:                       # optional registration metadata
  name: "Operaton Examples"
  description: "Curated examples demonstrating Operaton patterns."
  maintainer:
    name: "Karsten Thoms"
    url: "https://github.com/kthoms"

examples:                         # required, list
  - id: leave-request-spring-boot       # required, slug unique within repo
    title: "Leave Request (Spring Boot)" # required
    icon: "📝"                       # optional: emoji OR repo-relative image path
    path: examples/leave-request-spring-boot # required, relative to repo root
    shortDescription: >            # required, <= 200 chars
      Classic leave-request approval process with REST API and DMN-based policy.
    longDescription: |             # optional, markdown
      A complete leave-request workflow showing ...
    buildSystem: maven             # maven | gradle
    runtime: spring-boot           # spring-boot | quarkus | plain-java | other
    operatonVersion: "1.0.0-beta-5"
    javaVersion: "21"
    complexity: beginner           # beginner | intermediate | advanced
    tags:
      - { label: "Approval", category: "concept" }
      - { label: "DMN", category: "concept" }
    integrations: [rest, dmn]      # free-form strings
    bpmnConcepts: [user-task, exclusive-gateway, business-rule-task]
    requires: "Java 21+"
    authors:
      - { name: "Karsten Thoms", url: "https://github.com/kthoms" }
    license: "Apache-2.0"
    documentationUrl: "https://github.com/operaton/operaton-examples/blob/main/examples/leave-request-spring-boot/README.md"
    demoVideoUrl: null
    screenshots:
      - examples/leave-request-spring-boot/docs/process.png
    lastUpdated: "2026-06-10"
```

### Field reference

| Field | Required | Type | Notes |
|---|---|---|---|
| `apiVersion` | yes | string | Must start with `operaton-starter/v1` |
| `repository.name` | no | string | Display name for the source repo |
| `repository.description` | no | string | One-liner shown in source list / docs |
| `repository.maintainer.name` | no | string | |
| `repository.maintainer.url` | no | URL | |
| `examples[].id` | yes | slug | unique within the repo, `[a-z0-9-]+` |
| `examples[].title` | yes | string | |
| `examples[].icon` | no | emoji \| path | single emoji char, or repo-relative path to SVG/PNG ≤ 64×64 |
| `examples[].path` | yes | path | relative, no `..`, no leading `/` |
| `examples[].shortDescription` | yes | string ≤ 200 | |
| `examples[].longDescription` | no | markdown | |
| `examples[].buildSystem` | no | enum | `maven` \| `gradle` |
| `examples[].runtime` | no | enum | `spring-boot` \| `quarkus` \| `plain-java` \| `other` |
| `examples[].operatonVersion` | no | string | |
| `examples[].javaVersion` | no | string | |
| `examples[].complexity` | no | enum | `beginner` \| `intermediate` \| `advanced` |
| `examples[].tags[]` | no | `{label,category}` | reuses starter Tag model |
| `examples[].integrations[]` | no | string list | filterable |
| `examples[].bpmnConcepts[]` | no | string list | |
| `examples[].requires` | no | string | free-form prerequisites |
| `examples[].authors[]` | no | `{name,url?}` | |
| `examples[].license` | no | SPDX id | |
| `examples[].documentationUrl` | no | URL | |
| `examples[].demoVideoUrl` | no | URL | |
| `examples[].screenshots[]` | no | repo-relative paths | rendered in card detail |
| `examples[].lastUpdated` | no | ISO date | falls back to GitHub commit info |

Unknown fields are silently ignored.

## Implementation notes (file-level)

- **Backend**
  - New `org.operaton.dev.starter.server.examples.ExampleRepositoryLoader` (Spring `@Service`) — at `ApplicationReadyEvent`, for each configured source: (1) resolve `@ref` (or default branch) to a commit SHA via `GET /repos/{owner}/{repo}/commits/{ref}` with `Accept: application/vnd.github.sha`; (2) fetch `.operaton-starter.yml` at that SHA from `raw.githubusercontent.com`; (3) parse and validate. Stores an immutable in-memory snapshot per source. Sources fetched in parallel.
  - On manual refresh (`POST /api/v1/examples/refresh`), repeat the same flow; per-source success replaces that source's snapshot atomically. Per-source failure preserves the previous snapshot and is included in the response with reason.
  - YAML parsing: SnakeYAML with `SafeConstructor`; reject manifests > 256 KB.
  - Extend `StarterProperties` with a nested record `Examples(List<String> repositories, CacheConfig cache)` where `CacheConfig(Path dir, long maxSizeMb)`. Bind `STARTER_EXAMPLES_REPOSITORIES`, `STARTER_EXAMPLES_CACHE_DIR`, `STARTER_EXAMPLES_CACHE_MAXSIZEMB`.
  - Extend `MetadataController` to surface `examples` alongside `useCaseExamples`. Generated OpenAPI model gains a new `Example` schema (including `sourceRepoSha`, `icon`).
  - New `ExampleDownloadController` exposes `GET /api/v1/examples/{owner}/{repo}/{exampleId}/download`. Resolves pinned SHA from the in-memory snapshot. Cache lookup: if `{cacheDir}/{owner}/{repo}/{sha}/{exampleId}.zip` exists, stream it. Otherwise build it by reading `https://codeload.github.com/{owner}/{repo}/tar.gz/{sha}` via `Apache Commons Compress`, filtering entries under the example `path:`, re-packing as ZIP, writing to cache atomically (write to `.tmp` then rename), then stream.
  - LRU cache eviction: simple touch-on-read + periodic prune task that, when total cache size exceeds `maxSizeMb`, deletes oldest files (by last-access timestamp) until under threshold.
  - New `ExampleRefreshController` exposes `POST /api/v1/examples/refresh` returning the per-source load summary.
  - Diagnostics: `/actuator/examples` (custom endpoint) returns per-source load status (including resolved SHA, last-fetched timestamp, example count, last error).
- **Frontend**
  - Extend `useMetadata()` composable to expose `examples` array.
  - New `ExampleGalleryCard.vue` modeled on `UseCaseGalleryCard.vue`.
  - `GalleryView.vue` renders two `<section>` blocks with shared search + filter state in a Pinia store (or `provide/inject`).
  - Download action calls the new endpoint via `window.location.assign` (browser handles `Content-Disposition`).
- **OpenAPI**
  - Regenerate `UseCaseExample` neighbor model `Example`; CI must run the generator.
- **Tests**
  - Loader unit tests: well-formed, malformed YAML, missing required fields, oversize, unknown apiVersion, schema-violation skip.
  - WireMock-backed integration test for the download controller covering 200, 404 (unknown id), 502 (GitHub down).
  - Frontend component tests for `ExampleGalleryCard` and the shared search filtering both sections.

## Rejected alternatives

- **Periodic background refresh (v1):** rejected; manual refresh endpoint + restart cover the need without scheduler complexity.
- **Generic Git fetch (not GitHub-specific):** rejected for v1; GitHub-only keeps URL construction trivial and is sufficient for the seed source.
- **Auth on the refresh endpoint:** rejected for v1 (see Open Q-1); revisit if community deployments expose the starter.
- **Bypass `@ref` pinning (always fetch tip):** rejected — pinning makes the manifest view and the downloaded code agree even if the repo branch advances mid-session.
