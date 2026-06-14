---
baseline_commit:
---

# Story 8.2: Add Examples Configuration Properties and Manifest Parser

## Status
ready-for-dev

## Story

As a developer building operaton-starter,
I want `StarterProperties` extended with examples configuration and a safe YAML parser that validates manifests,
So that the loader can read a configured list of repositories and reject malformed or unsafe manifests before they enter the in-memory snapshot.

## Acceptance Criteria

1. Given `StarterProperties` is extended with a nested `Examples` record. When the application boots. Then `repositories: List<String>`, `cache.dir: Path` (default `${java.io.tmpdir}/operaton-starter/examples-cache`), `cache.maxSizeMb: long` (default 512), and `maxDownloadSizeMb: long` (default 50) are bound from `application.properties` and from the env vars `STARTER_EXAMPLES_REPOSITORIES`, `STARTER_EXAMPLES_CACHE_DIR`, `STARTER_EXAMPLES_CACHE_MAXSIZEMB`, `STARTER_EXAMPLES_MAXDOWNLOADSIZEMB`.
2. Given the default Spring properties ship preconfigured. When no environment overrides are present. Then `starter.examples.repositories` contains the single entry `kthoms/operaton-examples`.
3. Given a source token is invalid (does not match `^[A-Za-z0-9._-]+/[A-Za-z0-9._-]+(@[A-Za-z0-9._/-]+)?$`). When `StarterProperties` is validated at `@PostConstruct`. Then the invalid token is dropped, a startup warning is logged identifying the token, and the application boots normally.
4. Given `ExampleManifestParser` is invoked with a well-formed manifest. When parsing completes. Then the parser returns a populated `ParsedManifest` containing each example with its computed `sourceRepo` and `sourceRepoSha`; `apiVersion` is checked against the major prefix `operaton-starter/v1`; unknown fields are silently ignored.
5. Given `ExampleManifestParser` is invoked with a manifest > 256 KB, an unknown major `apiVersion`, an invalid `path` (absolute, contains `..`, or contains `\0`), or syntactically broken YAML. When parsing runs. Then the parser throws a typed `ManifestRejected` exception with a `reason` field; no partial result is returned.
6. Given ArchUnit test `NoArbitraryYamlInstantiationTest` is run. When scanning production sources. Then any `Yaml` construction outside test code that does not use `SafeConstructor` fails the build.

## Tasks/Subtasks

- [ ] Task 1: Extend `StarterProperties` with nested `Examples` record
  - [ ] 1.1: Add `repositories: List<String>` with default `kthoms/operaton-examples`
  - [ ] 1.2: Add `Cache` nested record with `dir: Path` (default `${java.io.tmpdir}/operaton-starter/examples-cache`) and `maxSizeMb: long` (default 512)
  - [ ] 1.3: Add `maxDownloadSizeMb: long` (default 50)
  - [ ] 1.4: Wire env var bindings: `STARTER_EXAMPLES_REPOSITORIES`, `STARTER_EXAMPLES_CACHE_DIR`, `STARTER_EXAMPLES_CACHE_MAXSIZEMB`, `STARTER_EXAMPLES_MAXDOWNLOADSIZEMB`
- [ ] Task 2: Add `@PostConstruct` validation to `StarterProperties` for source token format
  - [ ] 2.1: Token regex: `^[A-Za-z0-9._-]+/[A-Za-z0-9._-]+(@[A-Za-z0-9._/-]+)?$`
  - [ ] 2.2: Drop invalid tokens with warning log; app must boot regardless
- [ ] Task 3: Implement `ExampleManifestParser` using SnakeYAML `SafeConstructor`
  - [ ] 3.1: Set `LoaderOptions.codePointLimit` to 256 KB
  - [ ] 3.2: Check `apiVersion` starts with `operaton-starter/v1`; throw `ManifestRejected("api-version")` if not
  - [ ] 3.3: Validate each example `path`: no `..`, no leading `/`, no `\0`; throw `ManifestRejected("path-unsafe")` on violation
  - [ ] 3.4: Silently ignore unknown fields
  - [ ] 3.5: Set `sourceRepo` and `sourceRepoSha` as computed fields from the parser arguments
- [ ] Task 4: Define `ParsedManifest` record and `ManifestRejected` exception with `reason` field
- [ ] Task 5: Add `NoArbitraryYamlInstantiationTest` ArchUnit test
- [ ] Task 6: Write unit tests for parser: valid manifest, manifest > 256 KB, bad apiVersion, path traversal, broken YAML
- [ ] Task 7: Update `application.properties` with default `starter.examples.repositories=kthoms/operaton-examples`

## Dev Notes

- Architecture A3: Source tokens must match `^[A-Za-z0-9._-]+/[A-Za-z0-9._-]+(@[A-Za-z0-9._/-]+)?$`. Invalid tokens are dropped with a startup warning — the application never fails to boot.
- Architecture A4: YAML parser must use SnakeYAML `SafeConstructor` (no class instantiation), `LoaderOptions.codePointLimit` set to 256 KB. Unknown fields silently ignored.
- Architecture A8: `NoArbitraryYamlInstantiationTest` — any class loading user-supplied YAML must construct the parser with `SafeConstructor`. Negative test rejects `Yaml(new Constructor(...))` and `new Yaml()` outside test sources.
- Architecture A9: YAML safety is enforced at the parser level and verified by ArchUnit.
- Architecture A10: Never read user-supplied YAML with anything other than SnakeYAML `SafeConstructor`. Tested by ArchUnit (A8.2).
- This story has NO HTTP calls — fetcher is wired in Story 8.3. The parser takes raw YAML bytes as input.
- New classes go in `org.operaton.dev.starter.server.examples` package.

### Project Structure Notes

- `starter-server/src/main/java/org/operaton/dev/starter/server/config/StarterProperties.java` — extend with `Examples` record
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleManifestParser.java` — new
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ParsedManifest.java` — new record
- `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ManifestRejected.java` — new exception
- `starter-server/src/main/resources/application.properties` — add default repositories property
- `starter-server/src/test/java/.../examples/ExampleManifestParserTest.java` — new
- `starter-server/src/test/java/.../arch/NoArbitraryYamlInstantiationTest.java` — new ArchUnit test

### References

- [Source: docs/bmad/planning-artifacts/architecture.md#A3]
- [Source: docs/bmad/planning-artifacts/architecture.md#A4]
- [Source: docs/bmad/planning-artifacts/architecture.md#A8]
- [Source: docs/bmad/planning-artifacts/architecture.md#A9]
- [Source: docs/bmad/planning-artifacts/prds/prd-operaton-starter-examples-gallery-2026-06-13/addendum.md]

## Dev Agent Record

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
