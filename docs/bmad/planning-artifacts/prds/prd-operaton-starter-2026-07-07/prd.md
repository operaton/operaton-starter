---
title: "Examples Gallery: Repository-Wide Descriptor Discovery"
status: final
created: 2026-07-07
updated: 2026-07-07
parent_prd: "prd-operaton-starter-examples-gallery-2026-06-13"
---

# PRD — Examples Gallery: Repository-Wide Descriptor Discovery

## 1. Summary

This enhancement extends the Examples Gallery's repository scanning mechanism to discover `.operaton-starter.yml` (and `.yaml`) descriptor files anywhere in the repository tree, rather than assuming a single file at the root. It also makes the `path` field optional (defaulting to `.`), enabling per-directory descriptors to self-describe their containing directory without redundant path specification. Multiple descriptors per repository are fully supported.

## 2. Background

The Examples Gallery (see [parent PRD](../prd-operaton-starter-examples-gallery-2026-06-13/prd.md)) currently fetches a single `.operaton-starter.yml` manifest from the repository root. That file lists all examples, each with a `path` field pointing to the example subdirectory, relative to the repo root.

This design constrains repository authors: every example repository must maintain one central manifest at the root, even in monorepos where it is natural for each example subdirectory to own its own descriptor. It also forces root manifests to repeat long relative paths (e.g., `path: examples/leave-request-spring-boot`) that could simply be inferred from the descriptor's location. The `.yaml` extension, which is equally common and valid, is not currently recognized.

## 3. Goals

1. Allow descriptor files to be placed at any depth in the repository tree.
2. Support multiple descriptor files per repository.
3. Make `path` optional; default to `.` (the descriptor's own directory).
4. Recognize both `.yml` and `.yaml` file extensions.
5. Preserve full backward compatibility with existing root-level manifests.

## 4. Non-Goals

- Changing any descriptor fields beyond `path` optionality.
- Private repository authentication.
- Modifications to gallery rendering, filtering, or the download ZIP assembly logic (beyond path resolution).
- Handling repositories with no descriptor files differently from today (they remain silently empty sources).

## 5. Functional Requirements

### FR-1 Repository-Wide Scanning

**FR-1.1** The system MUST scan the entire repository file tree for descriptor files, replacing the current single-fetch of `<root>/.operaton-starter.yml`.

**FR-1.2** Scanning MUST use the GitHub Git Trees API (`GET /repos/{owner}/{repo}/git/trees/{sha}?recursive=1`) to enumerate all repository files in a single API round trip per source.

**FR-1.3** A file qualifies as a descriptor if and only if its filename is exactly `.operaton-starter.yml` or `.operaton-starter.yaml` (case-sensitive).

**FR-1.4** When the GitHub Trees API returns `truncated: true` (repository exceeds the API's single-response capacity), the system MUST log a warning identifying the source repository and process only the descriptors visible in the partial response. It MUST NOT drop the source entirely or fail silently.

### FR-2 Multiple Descriptors per Repository

**FR-2.1** A repository MAY contain zero, one, or many descriptor files at any depth.

**FR-2.2** Each discovered descriptor file MUST be fetched and parsed independently.

**FR-2.3** All examples from all descriptors within a repository MUST be combined into a single flat result for that source, exactly as if they had appeared in one root manifest.

**FR-2.4** Example `id` values MUST remain unique within a repository across all of its descriptors. If a duplicate `id` is detected across descriptors in the same repository, the system MUST log a warning and skip the duplicate (first-discovered wins). The discovery order follows the ordering returned by the GitHub Trees API.

### FR-3 Descriptor-Relative Path Resolution

**FR-3.1** The `path` field in each example entry MUST be interpreted as relative to the directory that contains the descriptor file, not relative to the repository root.

**FR-3.2** Resolved absolute path = `<descriptorDir>/<path>`, where `<descriptorDir>` is the repository-root-relative directory of the descriptor (empty string `""` for a root-level descriptor).

**FR-3.3** Backward compatibility is preserved automatically: a descriptor at the repository root has `<descriptorDir>` = `""`, so resolved paths equal the raw `path` value, unchanged from today's behavior.

### FR-4 Optional `path` Field

**FR-4.1** The `path` field in an example entry MUST be optional.

**FR-4.2** When `path` is absent or null, it MUST default to `.` — meaning the example's content occupies the same directory as the descriptor.

**FR-4.3** Existing `path` validation rules remain in force when a value is provided: no leading `/`, no `..` segments, no null bytes, no empty string after trimming.

### FR-5 YAML Extension Support

**FR-5.1** Both `.operaton-starter.yml` and `.operaton-starter.yaml` MUST be recognized as valid descriptor filenames.

**FR-5.2** If both `.operaton-starter.yml` and `.operaton-starter.yaml` exist in the same directory, the `.yml` file MUST be processed and the `.yaml` file MUST be skipped. A warning MUST be logged identifying the collision directory and source repository.

### FR-6 Per-Descriptor Error Isolation

**FR-6.1** A failure to fetch or parse any individual descriptor (HTTP error, malformed YAML, validation failure) MUST NOT prevent other descriptors in the same repository from being processed.

**FR-6.2** Per-descriptor failures MUST be surfaced in the source's outcome detail (descriptor path + failure reason) so operators can diagnose issues without digging into server logs.

## 6. Non-Functional Requirements

**NFR-1 API efficiency.** For a repository with N descriptors, the total GitHub API calls per source refresh is 1 (tree enumeration) + N (per-descriptor fetches). For single-descriptor repositories this is one extra round trip (the tree call) compared to today; this overhead is acceptable.

**NFR-2 Backward compatibility.** No changes to the descriptor YAML schema are breaking. All existing root-level manifests continue to work without modification. Making `path` optional is purely additive.

**NFR-3 Timeouts and retries.** The existing per-call timeout and `SourceUnavailable` handling apply to the new tree-enumeration call as well as individual descriptor fetches.

## 7. Documentation Changes

**FR-7.1** `docs/examples-repository-format.md` MUST be updated to reflect:
- Descriptor files may be placed at any directory depth, not only the root.
- `path` is optional; when omitted the example occupies the descriptor's directory.
- Both `.yml` and `.yaml` extensions are valid.
- A new per-directory descriptor example (no `path` field; the descriptor IS the example entry point).
- Confirmation that the root-manifest pattern remains fully supported.

## 8. Resolved Questions

**OQ-1 Truncated-tree fallback** *(resolved)*: Warning + partial processing is acceptable for v1. Expected example repositories are small; a non-recursive fallback is deferred to a future enhancement if needed.

**OQ-2 Same-directory extension collision** *(resolved)*: `.yml` takes precedence; `.yaml` is skipped with a warning (see FR-5.2).

## 9. Success Metrics

- A repository structured with per-directory descriptors (no root manifest) loads correctly, with each example resolved to its descriptor's directory.
- The existing `operaton/operaton-examples` root-manifest repository loads identically to today — zero behavioral change for current users.
- Integration tests cover: `.yml` extension, `.yaml` extension, per-directory descriptor with implicit `path`, multiple descriptors in one repo, duplicate-id handling, and failed-descriptor isolation.
- No regression in gallery load latency for single-descriptor repositories under normal conditions.
