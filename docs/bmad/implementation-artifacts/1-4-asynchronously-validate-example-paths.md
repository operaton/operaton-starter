---
baseline_commit:
---

# Story 1-4: Asynchronously Validate Example Paths

## Status
ready-for-dev

## Story

As a **a frontend developer rendering a gallery card**,
I want **to know if an example's ZIP can actually be downloaded before the user clicks Download**,
So that **I can disable the button gracefully and show a warning icon instead of a 404 error**.

## Acceptance Criteria

**Given** manifests are loaded and examples are in the registry  
**When** the loader completes manifest load  
**Then** the loader asynchronously (non-blocking) validates each example by making a HEAD request to the GitHub tarball endpoint  
**And** validation URL: `https://codeload.github.com/{owner}/{repo}/tar.gz/{sourceRepoSha}` with path filter (to check if example subpath exists)  
**And** `isDownloadable` field is initially set to `true` (optimistic) for all examples  
**And** as validation completes in the background, `isDownloadable` is updated to `true` (path exists) or `false` (path missing/network error)  
**And** validation does NOT block manifest availability — examples appear in the gallery immediately even if validation is still running  
**And** validation results are included in the load-status summary (FR-B8)  
**Integration test:** Mock tarball endpoint; return 200 for some examples, 404 for others; verify `isDownloadable` flag updates asynchronously.
---

## Tasks/Subtasks

- [ ] Implementation tasks to be defined based on story requirements

## Dev Notes

- See epic-level documentation in epics-examples-gallery-v1.md for full context
- All stories should follow the patterns established in Examples Gallery v1 PRD

## Dev Agent Record

### Completion Notes
(To be filled during implementation)

### File List
(To be filled during implementation)

### Change Log
(To be filled during implementation)
