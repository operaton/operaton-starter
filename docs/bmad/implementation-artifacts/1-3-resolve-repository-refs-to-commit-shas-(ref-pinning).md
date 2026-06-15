---
baseline_commit:
---

# Story 1-3: Resolve Repository Refs to Commit SHAs (Ref Pinning)

## Status
ready-for-dev

## Story

As a **a developer downloading an example**,
I want **all example content fetched at a consistent commit SHA**,
So that **the downloaded ZIP matches the manifest metadata exactly (no mid-session repo changes)**.

## Acceptance Criteria

**Given** a configured repository ref like `kthoms/operaton-examples@main` (or default branch if `@ref` omitted)  
**When** the loader initializes or manual refresh is triggered  
**Then** the loader calls GitHub commits API to resolve the ref to a concrete commit SHA  
**And** API call: `GET /repos/{owner}/{repo}/commits/{ref}` with `Accept: application/vnd.github.sha` header  
**And** the resolved SHA is stored in the ExampleRegistry and stamped onto each Example as `sourceRepoSha`  
**And** all subsequent manifest fetches and download operations in that load cycle use the pinned SHA, never the moving ref  
**And** if SHA resolution fails (404, network error), the source is skipped per FR-B7 (graceful degradation)  
**Unit test:** Mock commits API; verify SHA resolution; verify SHA is immutable across load cycle.
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
