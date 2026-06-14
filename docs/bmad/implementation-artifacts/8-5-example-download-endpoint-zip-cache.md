---
baseline_commit: 
---

# Story 8.5: Implement Example Download Endpoint with SHA-Keyed ZIP Cache

## Status
ready-for-dev

## Story

As an API consumer,
I want `GET /api/v1/examples/{owner}/{repo}/{exampleId}/download` to stream a ZIP of the example subfolder pinned to the SHA the user saw,
so that the downloaded archive always matches the metadata that surfaced it, and repeat downloads are fast.

## Acceptance Criteria

1. **Given** a request to download a known example **When** the cache holds `{cacheDir}/{owner}/{repo}/{sha}/{exampleId}.zip` **Then** the endpoint streams the file with `Content-Type: application/zip`, `Content-Disposition: attachment; filename="{exampleId}.zip"`, an `ETag: W/"sha-{shortSha}-{exampleId}"` header, and a `Last-Modified` header from the file mtime.

2. **Given** a request to download a known example **When** the cache is empty for that key **Then** `ZipBuilder` fetches `https://codeload.github.com/{owner}/{repo}/tar.gz/{sha}`, walks tar entries with Apache Commons Compress, filters to entries under the example's `path:`, re-packs them into a new `.tmp` ZIP, atomic-renames `.tmp` to the cache path, and streams the result.

3. **Given** a tarball where the filtered uncompressed payload would exceed `maxDownloadSizeMb` **When** the running counter trips the limit during streaming **Then** the build aborts, the `.tmp` file is deleted, and the endpoint returns `413 Payload Too Large` with a `ProblemDetail` body identifying the example.

4. **Given** a tar entry whose normalized path escapes the example subpath (e.g. contains `..` or is absolute) **When** `ZipBuilder` encounters it **Then** the build aborts, the `.tmp` file is deleted, and the endpoint returns `502 Bad Gateway` with a `ProblemDetail` describing "upstream archive failed path-safety check".

5. **Given** GitHub is unreachable and the cache holds no entry for the requested key **When** the request runs **Then** the endpoint returns `502 Bad Gateway` with a `ProblemDetail` body; the existing snapshot in `ExampleRegistry` is **not** invalidated.

6. **Given** a request for a `(owner, repo, exampleId)` not present in the current `ExampleRegistry` **When** the controller resolves it **Then** the endpoint returns `404 Not Found`; no GitHub call is made.

7. **Given** the SHA used by the download endpoint **When** the build runs **Then** the SHA comes from the in-memory `ExampleRegistry` snapshot — `ExampleDownloadController` never calls the commits API itself (verified by unit test that asserts no fetcher invocation on the download path).

8. **Given** the cache size on disk exceeds `cache.maxSizeMb` **When** the LRU pruning task runs (every 10 minutes via `@Scheduled`) **Then** oldest-by-last-access files are deleted until total size is below the threshold; in-flight writes (the `.tmp` files) are excluded from candidates.

9. **Given** two concurrent requests for the same uncached `(sha, exampleId)` **When** both reach `ZipBuilder` **Then** both succeed (each writes its own `.tmp` then atomic-renames; the final cache entry is consistent); no partial or corrupt ZIP can be served.

## Tasks/Subtasks

- [ ] Task 1: Implement `ZipBuilder` service
  - [ ] 1.1: Fetch `https://codeload.github.com/{owner}/{repo}/tar.gz/{sha}` as a stream
  - [ ] 1.2: Walk tar entries using Apache Commons Compress `TarArchiveInputStream`
  - [ ] 1.3: Filter entries to those under the example's `path:` value
  - [ ] 1.4: Per-entry path-safety check: no `..`, no absolute paths; on violation abort + delete `.tmp` + throw `PathSafetyException`
  - [ ] 1.5: Track running uncompressed size; if exceeds `maxDownloadSizeMb` abort + delete `.tmp` + throw `SizeLimitExceededException`
  - [ ] 1.6: Re-pack filtered entries into a new `ZipOutputStream` backed by a `.tmp` file
  - [ ] 1.7: Atomic rename `.tmp` → final cache path `{cacheDir}/{owner}/{repo}/{sha}/{exampleId}.zip`

- [ ] Task 2: Implement `ExampleZipCache` service
  - [ ] 2.1: Cache key: `{cacheDir}/{owner}/{repo}/{sha}/{exampleId}.zip`
  - [ ] 2.2: `getOrBuild(owner, repo, sha, exampleId, subPath)`: return cached file if present, else delegate to `ZipBuilder`
  - [ ] 2.3: LRU pruning `@Scheduled` task: runs every 10 minutes, prunes oldest-by-last-access until total cache size ≤ `cache.maxSizeMb`; excludes `.tmp` files from candidates

- [ ] Task 3: Implement `ExampleDownloadController` (`GET /api/v1/examples/{owner}/{repo}/{exampleId}/download`)
  - [ ] 3.1: Resolve `(owner, repo, exampleId)` against `ExampleRegistry`; return 404 if not found (no GitHub call)
  - [ ] 3.2: Delegate to `ExampleZipCache.getOrBuild()` using SHA from the registry snapshot
  - [ ] 3.3: Stream the ZIP file as response with `Content-Type: application/zip`, `Content-Disposition: attachment; filename="{exampleId}.zip"`, `ETag: W/"sha-{shortSha}-{exampleId}"`, `Last-Modified` from file mtime
  - [ ] 3.4: Map `SizeLimitExceededException` → 413 `ProblemDetail`
  - [ ] 3.5: Map `PathSafetyException` → 502 `ProblemDetail`
  - [ ] 3.6: Map GitHub unreachable → 502 `ProblemDetail`

- [ ] Task 4: Write tests
  - [ ] 4.1: Cache-hit test: assert file served directly without calling ZipBuilder
  - [ ] 4.2: Cache-miss happy path (WireMock): assert ZIP built and cached
  - [ ] 4.3: 413 test: synthetic tarball exceeding `maxDownloadSizeMb`
  - [ ] 4.4: 502 path-safety test: tarball with `..` entry
  - [ ] 4.5: 502 GitHub-unreachable test (WireMock)
  - [ ] 4.6: 404 unknown example test
  - [ ] 4.7: Unit test asserting `ExampleDownloadController` never invokes `GitHubManifestFetcher`
  - [ ] 4.8: Concurrency test: two simultaneous requests for same uncached entry both succeed

## Dev Notes

- Architecture A6: Full download cycle diagram — cache key is `{cacheDir}/{owner}/{repo}/{sha}/{exampleId}.zip`. SHA-keying gives natural invalidation: a refresh that bumps the SHA leaves old entries to age out via LRU.
- Architecture A6: Concurrent writers cannot collide because each `(sha, exampleId)` write goes via a unique `.tmp` then atomic rename.
- Architecture A6: ETag is `W/"sha-{shortSha}-{exampleId}"`. `Last-Modified` set to file mtime. SHA comes from `ExampleRegistry` — never re-resolved in the download path.
- Architecture A9: Path traversal check on both manifest `path:` (done in 8.2) and tarball entry names (done here). A path failure aborts build and returns 502.
- Architecture A10: `ExampleDownloadController` never calls the commits API itself — SHA must come from the in-memory `ExampleRegistry` snapshot.
- Architecture A7: Endpoint is `GET /api/v1/examples/{owner}/{repo}/{id}/download` — no auth in v1.
- Apache Commons Compress is the required library for tar processing — verify it's in `starter-server/pom.xml`; add dependency if missing.
- `@Scheduled` pruning task: use `Files.walk` snapshot + best-effort delete; no global lock needed.
- New classes in `org.operaton.dev.starter.server.examples` package (ZipBuilder, ExampleZipCache) and `org.operaton.dev.starter.server.examples.api` (ExampleDownloadController).

### Project Structure Notes

- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ZipBuilder.java` — new
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleZipCache.java` — new
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/api/ExampleDownloadController.java` — new
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/SizeLimitExceededException.java` — new
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/PathSafetyException.java` — new
- `starter-server/pom.xml` — add Apache Commons Compress if not present

### References

- [Source: docs/bmad/planning-artifacts/architecture.md#A6]
- [Source: docs/bmad/planning-artifacts/architecture.md#A7]
- [Source: docs/bmad/planning-artifacts/architecture.md#A9]
- [Source: docs/bmad/planning-artifacts/architecture.md#A10]

## Dev Agent Record

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
