---
baseline_commit:
---

# Story 8.3: Implement GitHub Manifest Fetcher

## Status
ready-for-dev

## Story

As a developer building operaton-starter,
I want a `GitHubManifestFetcher` that resolves source tokens to manifests pinned at a commit SHA,
So that what the user sees in the gallery and what they download are guaranteed to come from the same commit.

## Acceptance Criteria

1. Given a source token `owner/repo` (no `@ref`). When `GitHubManifestFetcher.fetch()` runs. Then it issues `GET https://api.github.com/repos/{owner}/{repo}/commits/HEAD` with `Accept: application/vnd.github.sha`, captures the returned 40-character SHA, then issues `GET https://raw.githubusercontent.com/{owner}/{repo}/{sha}/.operaton-starter.yml` and returns both the YAML bytes and the resolved SHA.
2. Given a source token `owner/repo@some-branch`. When the fetch runs. Then the commits-API call uses `{ref}` = `some-branch`; the raw-content URL uses the resolved SHA, never the ref string.
3. Given any HTTP call to GitHub exceeds 5 seconds. When the timeout fires. Then the fetcher throws `SourceUnavailable("timeout")`; no partial result is returned.
4. Given the commits API returns 4xx or 5xx, or the raw URL returns non-200. When the fetcher runs. Then it throws `SourceUnavailable("http-{status}")`; the response body is not parsed.
5. Given WireMock-backed tests. When the suite runs. Then there is at least one test per outcome: 200 happy path, 404 on commits, 404 on raw, 5xx, network timeout, and a manifest with a non-default branch ref.

## Tasks/Subtasks

- [ ] Task 1: Implement `GitHubManifestFetcher` service class
  - [ ] 1.1: Parse source token `owner/repo` or `owner/repo@ref` — default ref is `HEAD`
  - [ ] 1.2: Call `GET https://api.github.com/repos/{owner}/{repo}/commits/{ref}` with `Accept: application/vnd.github.sha`; parse 40-char SHA from response
  - [ ] 1.3: Call `GET https://raw.githubusercontent.com/{owner}/{repo}/{sha}/.operaton-starter.yml`
  - [ ] 1.4: Return `FetchResult(yamlBytes: byte[], resolvedSha: String)`
  - [ ] 1.5: 5-second per-call timeout; throw `SourceUnavailable("timeout")` on timeout
  - [ ] 1.6: On any non-2xx, throw `SourceUnavailable("http-{status}")`
- [ ] Task 2: Define `SourceUnavailable` exception with `reason` field
- [ ] Task 3: Define `FetchResult` record
- [ ] Task 4: Write WireMock-backed integration tests covering: happy path, 404 on commits, 404 on raw, 5xx, timeout, non-default branch ref

## Dev Notes

- Architecture A5: SHA resolution via `GET /repos/{o}/{r}/commits/{ref}` with `Accept: application/vnd.github.sha`; raw manifest via `raw.githubusercontent.com/{o}/{r}/{sha}/.operaton-starter.yml`.
- Architecture A9: Outbound surface limited to `raw.githubusercontent.com`, `api.github.com`. No credentials.
- Architecture A11: Chosen approach resolves `@ref` → SHA at each load (not always-fetch-tip) for manifest/download consistency.
- Use Java's `HttpClient` (built-in, Java 11+) or Spring's `RestClient`/`WebClient` following the existing project pattern. Check existing HTTP client usage in `MetadataController` or other services.
- WireMock is already in the project's test dependencies (check `pom.xml` in `starter-server`).
- The fetcher is a pure infrastructure class — it calls the parser in Story 8.2 separately; keep the two decoupled.
- New class in `org.operaton.dev.starter.server.examples` package.

### Project Structure Notes

- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/GitHubManifestFetcher.java` — new
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/FetchResult.java` — new record
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/SourceUnavailable.java` — new exception
- `starter-server/src/test/java/.../examples/GitHubManifestFetcherTest.java` — new WireMock test

### References

- [Source: docs/bmad/planning-artifacts/architecture.md#A5]
- [Source: docs/bmad/planning-artifacts/architecture.md#A9]
- [Source: docs/bmad/planning-artifacts/architecture.md#A11]

## Dev Agent Record

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
