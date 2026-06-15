---
baseline_commit:
---

# Story 3-6: Integration Test — Download Endpoint & Cache

## Status
ready-for-dev

## Story

As a **a backend developer validating the download pipeline**,
I want **to verify that downloads work end-to-end: fetch tarball, pack ZIP, cache, serve**,
So that **the feature is production-ready and resilient to edge cases**.

## Acceptance Criteria

**Given** examples are loaded (from Epic 1) and the download endpoint is implemented  
**When** a download request is made for an example  
**Then** the full pipeline works:
  1. Endpoint resolves example to pinned SHA
  2. Fetches GitHub tarball: `https://codeload.github.com/{owner}/{repo}/tar.gz/{sha}`
  3. Filters tarball to example subpath
  4. Re-packs as ZIP
  5. Writes to cache: `{cache-dir}/{owner}/{repo}/{sha}/{exampleId}.zip`
  6. Streams ZIP to client
  7. Returns 200 OK + ZIP file
**When** a second request for the SAME `(owner, repo, sha, exampleId)` is made  
**Then** the cached ZIP is served directly (no GitHub fetch)  
**And** access time is updated in LRU tracker  
**When** a request is made for a DIFFERENT example  
**Then** a new ZIP is cached separately  
**When** the cache exceeds max size  
**Then** least recently accessed ZIPs are evicted  
**When** the example path does not exist  
**Then** a 404 is returned with structured error body  
**When** the ZIP would exceed 50 MB  
**Then** a 413 is returned; partial cache file is deleted  
**When** GitHub is unreachable  
**Then** a 502 is returned  
**Integration test (TestRestTemplate + WireMock):**
1. Mock GitHub tarball endpoint
2. Call download → verify 200, file received, cache written
3. Call download again → verify cache hit (no new GitHub call)
4. Trigger 404 scenario → verify 404 response + no cache
5. Trigger 413 scenario → verify 413 response + cache cleaned up
6. Trigger GitHub down → verify 502 response
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
