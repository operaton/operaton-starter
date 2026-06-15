---
baseline_commit:
---

# Story 1-5: Implement Manual Refresh Endpoint

## Status
ready-for-dev

## Story

As a **a system operator**,
I want **to refresh examples without restarting the server**,
So that **new examples from a repository are available immediately when I update the manifest**.

## Acceptance Criteria

**Given** the application is running with loaded examples  
**When** a POST request is made to `/api/v1/examples/refresh`  
**Then** the endpoint triggers the same manifest + validation flow as startup (load, pin SHAs, validate asynchronously)  
**And** on success, the in-memory ExampleRegistry snapshot is replaced atomically  
**And** the endpoint returns the per-source load-status summary (same format as FR-B8 diagnostics)  
**And** if a source fails during refresh, the previous snapshot for that source is preserved (FR-B7)  
**And** the endpoint response includes timestamps showing when each source was last refreshed  
**HTTP Response (200):**
```json
{
  "status": "refresh_complete",
  "sources": [
    {
      "repo": "kthoms/operaton-examples",
      "status": "loaded",
      "exampleCount": 12,
      "resolvedSha": "abc1234567890abcdef",
      "lastFetched": "2026-06-15T14:30:00Z"
    },
    {
      "repo": "acme/operaton-samples",
      "status": "skipped",
      "reason": "manifest_parse_error",
      "lastFetched": "2026-06-15T14:29:45Z"
    }
  ]
}
```
**Integration test:** Trigger refresh; modify mock manifest; verify new examples appear; trigger refresh again with failed source; verify previous snapshot preserved.
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
