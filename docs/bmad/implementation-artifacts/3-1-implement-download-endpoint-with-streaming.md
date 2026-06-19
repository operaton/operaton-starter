---
baseline_commit:
---

# Story 3-1: Implement Download Endpoint with Streaming

## Status
ready-for-dev

## Story

As a **a developer downloading an example**,
I want **to click Download and receive a ZIP file streamed from the server**,
So that **I can extract the example code and start working immediately**.

## Acceptance Criteria

**Given** an example exists in a registered repository  
**When** a request is made to `GET /api/v1/examples/{owner}/{repo}/{exampleId}/download`  
**Then** the endpoint:
  - Resolves `{owner}/{repo}/{exampleId}` to the pinned SHA from the ExampleRegistry
  - Fetches the GitHub tarball: `https://codeload.github.com/{owner}/{repo}/tar.gz/{sha}`
  - Filters the tarball to the example's subpath (e.g., `examples/kafka-saga/`)
  - Re-packs filtered content as a ZIP file
  - Streams the ZIP to the client  
  - Sets response headers: `Content-Type: application/zip`, `Content-Disposition: attachment; filename="{exampleId}.zip"`  
**And** the endpoint is routed separately from static resources (ensures proper error handling, not caught by static handler)  
**And** streaming is used throughout (no large in-memory buffering)  
**When** the download completes successfully  
**Then** a 200 OK response is returned with the ZIP file  
**When** the example path does not exist at the pinned SHA  
**Then** the endpoint returns 404 with structured error body:
```json
{
  "status": "not_found",
  "error": "Example path does not exist in repository",
  "details": {
    "sourceRepo": "operaton/operaton-examples",
    "sourceSha": "abc1234567890abcdef",
    "exampleId": "kafka-saga",
    "expectedPath": "examples/kafka-saga",
    "sourceRepoUrl": "https://github.com/operaton/operaton-examples/tree/abc1234567890abcdef/examples/kafka-saga"
  }
}
```
**When** GitHub is unreachable (network timeout, 5xx error)  
**Then** the endpoint returns 502 Bad Gateway with error body  
**When** the example ZIP exceeds 50 MB uncompressed  
**Then** the endpoint aborts, deletes the partial cache file, and returns 413 Payload Too Large with error body  
**Integration test:** Mock GitHub tarball; test success path (200), missing path (404), oversized ZIP (413), network error (502).
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
