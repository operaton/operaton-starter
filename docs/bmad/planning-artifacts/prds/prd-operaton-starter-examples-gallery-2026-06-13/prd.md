---
title: "Examples Gallery: Remote Example Repositories"
status: final
created: 2026-06-13
updated: 2026-06-13
---

# PRD — Examples Gallery: Remote Example Repositories

## 1. Summary

Add an **Examples** section to the operaton-starter gallery that lists ready-to-download example projects sourced from external GitHub repositories. Each repository publishes a `.operaton-starter.yml` manifest describing one or more examples (title, descriptions, build system, runtime, integrations, etc.). The starter aggregates manifests from a maintainer-controlled list of repositories, displays examples in a searchable/filterable gallery alongside (not replacing) the existing Use Cases, and lets users download an example as a ZIP with one click.

The first source repository is **`kthoms/operaton-examples`**. The repository registration format is documented in user-facing docs and linked from the gallery.

## 2. Goals & Non-Goals

### Goals
- Grow the catalog of working Operaton examples without redeploying the starter for every new example.
- Give users enough metadata up front (build system, runtime, integrations, complexity) to know what to expect before downloading.
- Make example download as frictionless as generating a new project (one click → ZIP).
- Establish a documented, stable manifest format third-party authors can target.

### Non-Goals (v1)
- User-configurable repository list in UI. The list is curated by maintainers via configuration.
- Authentication for private GitHub repositories. Public repos only.
- Replacing the built-in Use Cases. Examples and Use Cases coexist; deprecation is a future decision.
- Running, validating, or testing examples server-side. Trust is on the example author.
- Versioning/branches per example. v1 always reads the default branch.

## 3. Users & Context

Primary users are Operaton evaluators and developers who land on the starter looking for a working starting point. Today they see four built-in use cases; this is too narrow and grows only by changes to the starter codebase. Example authors (community contributors, including Karsten maintaining `kthoms/operaton-examples`) want to publish examples without touching the starter.

Stakes: **internal / project-team**. Single Operaton maintainer team owns both the starter and the seed example repository.

## 4. User Journeys

**UJ-1 — Developer browses for an example.**
Anna opens the starter, clicks "Gallery", scrolls past the existing Use Cases section, lands on the new "Examples" section. She types "kafka" into the gallery search box; the list filters to examples tagged with a Kafka integration. She expands one card, reads the long description and the integration list, and clicks "Download ZIP". The starter streams a ZIP of the example subfolder; she unzips and runs `mvn spring-boot:run`.

**UJ-2 — Example author publishes a new example.**
Karsten adds a new subfolder `examples/payment-approval/` to `kthoms/operaton-examples` and appends an entry to the repo's root `.operaton-starter.yml`. He pushes to `main`. The next time an operaton-starter server boots, the new example appears in the gallery.

**UJ-3 — Operator adds a new source repo.**
The Operaton maintainer team wants to onboard `acme/operaton-bpmn-samples`. They append the repo URL to `STARTER_EXAMPLES_REPOSITORIES` env var (or `application.properties`) and restart the starter. The repo's manifest is fetched at startup, parsed, and its examples join the gallery.

**UJ-4 — A source repo is broken.**
A repository's manifest has a YAML syntax error. On startup the loader logs a warning naming the repo and skips it. The gallery still shows examples from all other sources. The admin can inspect logs to learn what failed.

## 5. Functional Requirements

### FR Group A — Manifest Schema & Repository Layout
- **FR-A1** Define a `.operaton-starter.yml` manifest format placed at the **root** of an example repository.
- **FR-A2** The manifest declares a list of examples; each example references a subdirectory via `path:` (relative to repo root).
- **FR-A3** Each example entry **MUST** provide: `id` (slug, unique within repo), `title`, `shortDescription` (≤ 200 chars), `path`.
- **FR-A4** Each example entry **MAY** provide: `longDescription` (markdown), `tags` (list of `{label, category}`), `buildSystem` (`maven`|`gradle`), `runtime` (`spring-boot`|`quarkus`|`plain-java`|`other`), `operatonVersion`, `javaVersion`, `integrations` (list of strings, e.g. `kafka`, `rest`, `postgres`, `dmn`, `external-task`), `bpmnConcepts` (list of strings), `complexity` (`beginner`|`intermediate`|`advanced`), `authors` (list of `{name, url?}`), `license`, `documentationUrl`, `demoVideoUrl`, `lastUpdated` (ISO date; if absent, derived from GitHub commit info), `requires` (free-form prerequisites string, e.g. "Docker, Postgres 16"), `screenshots` (list of repo-relative image paths), `icon` (an emoji character **or** a repo-relative path to an SVG/PNG ≤ 64×64; displayed on the gallery card; falls back to a generic icon if absent).
- **FR-A5** A `repository` block at manifest root **MAY** provide registration metadata: `name`, `description`, `maintainer` (`{name, url?}`).
- **FR-A6** Unknown fields are ignored, not rejected — the schema is forward-compatible.
- **FR-A7** Manifest version is declared via `apiVersion: operaton-starter/v1` at the top; loaders refuse manifests with unknown major versions and log a warning.

### FR Group B — Repository Registration & Loading
- **FR-B1** The starter holds a **maintainer-controlled** list of example-repository sources. v1 source is a configuration property, not a UI surface.
- **FR-B2** Each registered source is a GitHub repository identifier of the form `owner/repo[@ref]`. If `@ref` is omitted, the repo's default branch is used.
- **FR-B3** v1 ships with **`kthoms/operaton-examples`** preconfigured.
- **FR-B4** Repository sources are configured via Spring properties (`starter.examples.repositories[]`) and overridable via `STARTER_EXAMPLES_REPOSITORIES` env var (comma-separated).
- **FR-B5** Manifests are fetched at server startup and on demand via a manual refresh endpoint (FR-B9). There is no automatic periodic refresh in v1.
- **FR-B6** Manifests are fetched via the GitHub raw content URL for the configured ref (e.g. `https://raw.githubusercontent.com/{owner}/{repo}/{ref}/.operaton-starter.yml`). No GitHub API token required for public repos.
- **FR-B7** If a manifest fails to load (network error, 404, YAML parse error, schema violation, unknown apiVersion), the loader **logs a warning** identifying the source and reason, then **skips** that source. Other sources continue to load. The startup never fails because of a remote source. On manual refresh, a failed source preserves its **previous** snapshot rather than disappearing from the gallery.
- **FR-B8** The loader exposes a structured load-status summary (per source: `loaded` | `skipped: <reason>`, count of examples, last-fetched timestamp, resolved SHA) accessible via an actuator-style internal endpoint for diagnostics. `[ASSUMPTION]` reusing Spring Boot Actuator already on the classpath.
- **FR-B9** **Manual refresh.** `POST /api/v1/examples/refresh` re-fetches every registered source's manifest and replaces the in-memory snapshot atomically on success. The endpoint returns the same per-source summary as FR-B8. v1 does not gate the endpoint with auth `[ASSUMPTION]` — the starter is typically run locally or behind a maintainer-controlled deployment; if deployed publicly, the operator restricts it via reverse proxy. `[NOTE FOR PM]` revisit if community deployments emerge.
- **FR-B10** **Ref pinning.** At each load (startup or manual refresh), the loader resolves the configured `@ref` (or default branch) to a concrete **commit SHA** via the GitHub commits API (`GET /repos/{owner}/{repo}/commits/{ref}` with `Accept: application/vnd.github.sha`). All manifest and example-content fetches in that load cycle use the resolved SHA, not the moving ref. The resolved SHA is exposed in load status and stamped onto every example as `sourceRepoSha`. Downloads also use the pinned SHA (see FR-C3). Pinning isolates the user view from mid-session repo changes.

### FR Group C — Backend API
- **FR-C1** Extend the existing `GET /api/v1/metadata` response with an `examples` list parallel to `useCaseExamples`. Each entry exposes the manifest fields above plus computed fields: `sourceRepo` (the `owner/repo[@ref]`), `sourceRepoSha` (pinned commit SHA from FR-B10), `sourceRepoUrl` (HTML URL to the example folder at the pinned SHA).
- **FR-C2** Add `GET /api/v1/examples/{owner}/{repo}/{exampleId}/download` returning a ZIP of the example's subdirectory content (`application/zip`, `Content-Disposition: attachment; filename="{exampleId}.zip"`).
- **FR-C3** The download endpoint resolves `{owner}/{repo}/{exampleId}` to its **pinned SHA** (FR-B10) and serves a ZIP of the example's subpath at that SHA. The starter caches downloaded ZIPs server-side (see FR-C6); the first request builds the ZIP by reading the GitHub tarball (`https://codeload.github.com/{owner}/{repo}/tar.gz/{sha}`), filtering to the example subpath, re-packing as ZIP, and writing to cache. Subsequent requests stream the cached file. Streaming avoids large in-memory buffering on first build.
- **FR-C4** If the source repo or example id is unknown, the download endpoint returns `404`. If GitHub is unreachable on a cache miss, it returns `502` with a structured error body. A cache hit succeeds regardless of GitHub availability.
- **FR-C5** Manifest content is kept in memory after the most recent successful load (startup or refresh). It is not persisted to disk.
- **FR-C6** **ZIP cache.** Downloaded example ZIPs are cached on disk under `${java.io.tmpdir}/operaton-starter/examples-cache/{owner}/{repo}/{sha}/{exampleId}.zip` (path overridable via `starter.examples.cache.dir`). Cache key includes the resolved SHA, so a refresh that bumps the SHA naturally invalidates old entries. Bounded by total size `starter.examples.cache.maxSizeMb` (default 512 MB) with LRU eviction. The manual refresh endpoint (FR-B9) does **not** flush the cache — old SHA entries age out via LRU.
- **FR-C7** **Download telemetry.** The download endpoint increments an in-process counter per `(sourceRepo, exampleId)` and exposes the snapshot via `/actuator/examples`. Counters are not persisted across restarts. No PII, no remote reporting. This is the data source for SM-3.

### FR Group D — Frontend Gallery UI
- **FR-D1** The existing `GalleryView.vue` renders **two labeled subsections** on one page: **"Examples"** (new, listed first) and **"Use Cases"** (existing, below). A short blurb introduces each.
- **FR-D2** A new `ExampleGalleryCard.vue` renders each example: **icon** (emoji or fetched image; falls back to a default), title, shortDescription, tag chips, runtime/build-system badges, complexity badge, "Download ZIP" primary action, "View on GitHub" secondary action, and an expandable section showing longDescription (rendered markdown), integrations, bpmnConcepts, requires, authors, license, lastUpdated, and the pinned commit SHA (short form) as a small footer label.
- **FR-D3** A single search box at the top of the gallery filters **both** sections by title, description, tags, integrations.
- **FR-D4** Filter chips let users narrow by `runtime`, `buildSystem`, `complexity`, and `integrations`. **These chips apply only to the Examples subsection**; the Use Cases subsection is unaffected by them and keeps its existing filter behavior. The free-text search box (FR-D3) is the only control that filters both subsections simultaneously.
- **FR-D5** When **no examples** load (empty list or all sources failed), the Examples section renders an empty state explaining how to register a repo and links to the docs.
- **FR-D6** "Download ZIP" triggers a browser download via the new endpoint; while in progress, the button shows a spinner. On failure, an inline error toast surfaces the cause.
- **FR-D7** Tag chips reuse the existing `Tag` model and `tagColors.ts` styling; new categories (`runtime`, `integration`, `concept`) are added to `TagCategory` if not present.

### FR Group E — Documentation
- **FR-E1** Add `docs/examples-repository-format.md` documenting: rationale, full `.operaton-starter.yml` schema with field-by-field reference, a complete annotated example, instructions for getting a repo added to the registry (open a PR against the starter to extend the default list, or run with `STARTER_EXAMPLES_REPOSITORIES`), and forward-compatibility expectations.
- **FR-E2** The Examples section in the gallery includes a link "Publish your own examples →" pointing to that doc (rendered from the deployed site path).
- **FR-E3** The main README links to `docs/examples-repository-format.md` under a "Contributing examples" section.
- **FR-E4** The doc is the source of truth for the manifest schema. The PRD references but does not duplicate it long-term.

## 6. Non-Functional Requirements

- **NFR-1 Performance.** Total startup overhead added by example loading is ≤ 3s for N ≤ 10 sources on a normal network, since manifests + SHA resolution are fetched in parallel. Loader uses a hard per-source timeout of 5s; sources exceeding it are treated as failures per FR-B7.
- **NFR-2 Resilience.** Failure of any single source never blocks startup or breaks the gallery (see FR-B7).
- **NFR-3 Security.** Only public GitHub HTTPS endpoints are contacted. No credentials sent. ZIP downloads stream remote content; the server does not execute or evaluate it. Manifest parsing uses a YAML parser configured to **disallow arbitrary class instantiation** (SnakeYAML SafeConstructor or equivalent).
- **NFR-4 Size limits.** Manifests larger than 256 KB are rejected with a logged warning. Per-example uncompressed payload is capped at **50 MB** (configurable via `starter.examples.maxDownloadSizeMb`). If exceeded during tarball filtering, the build aborts, the partial cache file is deleted, and the endpoint returns `413 Payload Too Large` with a structured error body. Streaming is used throughout to avoid OOM.
- **NFR-5 Compatibility.** The existing `/api/v1/metadata` response stays backwards-compatible — `examples` is an additive field; existing clients ignore it.
- **NFR-6 Observability.** Per-source load result and per-download attempts are logged at INFO with structured fields (`source`, `exampleId`, `outcome`, `durationMs`).
- **NFR-7 Forward compatibility.** Unknown manifest fields are ignored; `apiVersion` is checked on major only.

## 7. Success Metrics

- ≥ 3 community-contributed examples published in `kthoms/operaton-examples` within 90 days of release.
- ≥ 1 third-party repository registered within 6 months.
- Examples ZIP download is used at least as often as use-case generation within 6 months (telemetry permitting).
- **Counter-metrics:** zero increase in starter startup failures attributable to example loading; ≤ 1% of `/api/v1/metadata` responses are "degraded," where **degraded** means the `examples` field is empty or missing while at least one source is configured (i.e. all configured sources failed to load).

## 8. Risks & Open Questions

- **R-1** GitHub rate-limiting on `raw.githubusercontent.com` and `codeload.github.com`. Anonymous limits are generous for read; mitigation if hit: introduce server-side ZIP caching keyed by `(source, ref-sha, exampleId)`. Out of scope for v1.
- **R-2** Malicious manifest content (e.g., huge `longDescription`, billions-of-laughs YAML). Mitigated by SafeConstructor + 256 KB cap.
- **R-3** Example authors break the schema. Mitigated by detailed docs + permissive parser + skip-with-warning.
- **R-4** Operaton-version drift between examples and the starter. Examples carry `operatonVersion`; UI surfaces it on the card so users see compatibility risks. No auto-blocking in v1.
- **Open Q-1** Should the manual refresh endpoint require authentication? v1 default: no. Revisit if the starter is commonly exposed to untrusted networks.

## 9. Phasing

- **v1 (this PRD):** Preconfigured `kthoms/operaton-examples`, env-overridable list. Startup fetch + manual refresh endpoint. Ref pinning to commit SHA. Server-side ZIP cache. Gallery subsection. Docs.
- **v2 (future):** UI to register/manage sources; deprecate built-in use cases (migrate to the same manifest format hosted in an Operaton-owned examples repo); per-example version pinning per user; private-repo auth; admin UI for diagnostics.

## 10. Appendix A — Manifest Schema (Reference Skeleton)

See `addendum.md` for the full schema and the sample `.operaton-starter.yml` for `kthoms/operaton-examples`.

## 11. Implementation Notes (Pointer)

Concrete file-level integration notes (where to extend `MetadataController`, the new loader service, ZIP streaming approach, properties record changes) live in `addendum.md` so the PRD stays capability-focused.
