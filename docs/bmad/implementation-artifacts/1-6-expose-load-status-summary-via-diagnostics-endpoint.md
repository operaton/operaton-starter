---
baseline_commit:
---

# Story 1-6: Expose Load-Status Summary via Diagnostics Endpoint

## Status
ready-for-dev

## Story

As a **a system operator troubleshooting a broken gallery**,
I want **to see which example sources loaded successfully and which failed**,
So that **I can diagnose connectivity or manifest issues without reading logs**.

## Acceptance Criteria

**Given** examples have been loaded at startup or via manual refresh  
**When** a request is made to the diagnostics endpoint (via Spring Boot Actuator)  
**Then** a `/actuator/examples` endpoint exposes a per-source summary including:
- `status`: `loaded` or `skipped: <reason>`
- `exampleCount`: number of examples from this source
- `lastFetched`: ISO timestamp of last successful/attempted load
- `resolvedSha`: the pinned commit SHA for this source (or null if load failed)  
**Example response:**
```json
{
  "timestamp": "2026-06-15T14:30:00Z",
  "sources": [
    {
      "repo": "kthoms/operaton-examples",
      "status": "loaded",
      "exampleCount": 12,
      "resolvedSha": "abc1234567890abcdef",
      "lastFetched": "2026-06-15T14:30:00Z"
    },
    {
      "repo": "broken-repo/operaton-examples",
      "status": "skipped",
      "reason": "network_timeout",
      "resolvedSha": null,
      "lastFetched": "2026-06-15T14:29:00Z"
    }
  ]
}
```
**Unit test:** Mock multiple sources (success + failures); verify endpoint response includes all sources with correct statuses.
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
