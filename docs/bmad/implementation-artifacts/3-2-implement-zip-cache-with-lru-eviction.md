---
baseline_commit:
---

# Story 3-2: Implement ZIP Cache with LRU Eviction

## Status
ready-for-dev

## Story

As a **a system operator managing server disk usage**,
I want **downloaded ZIPs to be cached and reused on subsequent requests**,
So that **repeated downloads of the same example don't re-fetch from GitHub and stay within disk limits**.

## Acceptance Criteria

**Given** the first download request for an example  
**When** the download endpoint builds a ZIP and caches it  
**Then** the ZIP is written to disk at: `{java.io.tmpdir}/operaton-starter/examples-cache/{owner}/{repo}/{sha}/{exampleId}.zip`  
**And** the path is configurable via `starter.examples.cache.dir` property  
**When** a subsequent request for the same `(owner, repo, sha, exampleId)` is made  
**Then** the cached ZIP is served directly from disk (no GitHub fetch, no re-packing)  
**And** the cached ZIP is streamed to the client (not loaded fully into memory)  
**When** the total cache size exceeds `starter.examples.cache.maxSizeMb` (default 512 MB)  
**Then** LRU eviction removes the least recently accessed entries until cache size is below the limit  
**And** access time is tracked per ZIP file (time of last read)  
**When** the pinned SHA changes (e.g., via manual refresh to a new ref)  
**Then** old SHA entries in the cache are naturally evicted over time via LRU  
**And** new examples at the new SHA use a new cache key, so no conflicts  
**When** a download fails (404, 502, 413)  
**Then** NO partial cache file is left behind (any written file is deleted)  
**Cache invariant:**
- Cache keys are immutable: `(owner, repo, sha, exampleId)` uniquely identifies a ZIP
- SHAs are pinned, so cached ZIPs never go stale
- LRU eviction handles size bounds without manual intervention
**Unit test:** Mock cache directory; test cache hit, cache miss, eviction, stale entry cleanup.
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
