---
baseline_commit:
---

# Story 1-8: Extend `/api/v1/metadata` with Examples List

## Status
ready-for-dev

## Story

As a **a frontend developer building the gallery**,
I want **to fetch the full example list from `/api/v1/metadata` alongside project types**,
So that **I have a single API contract for all gallery content**.

## Acceptance Criteria

**Given** examples have been loaded into the ExampleRegistry  
**When** a request is made to `GET /api/v1/metadata`  
**Then** the response includes an `examples` array (in addition to existing `projectTypes`)  
**And** each example in the array includes: all manifest fields plus computed fields: `sourceRepo`, `sourceRepoSha`, `sourceRepoUrl`, `isDownloadable`  
**And** `sourceRepoUrl` is an HTML GitHub URL to the example folder at the pinned SHA (e.g., `https://github.com/operaton/operaton-examples/tree/abc1234567890abcdef/examples/kafka-saga`)  
**And** if no examples are loaded, the `examples` array is empty (never null)  
**And** existing clients that ignore the `examples` field continue to work unchanged (backward compatibility per NFR-5)  
**Example response:**
```json
{
  "projectTypes": [ /* existing array */ ],
  "examples": [
    {
      "id": "kafka-saga",
      "title": "Kafka Saga Pattern",
      "shortDescription": "Example showing saga pattern with Kafka",
      "path": "examples/kafka-saga",
      "tags": [{ "label": "kafka", "category": "INTEGRATION" }],
      "complexity": "intermediate",
      "sourceRepo": "operaton/operaton-examples",
      "sourceRepoSha": "abc1234567890abcdef",
      "sourceRepoUrl": "https://github.com/operaton/operaton-examples/tree/abc1234567890abcdef/examples/kafka-saga",
      "isDownloadable": true,
      // ... all other manifest fields
    }
  ]
}
```
**Integration test:** Load examples; call `/api/v1/metadata`; verify response structure; verify all fields present; verify backward compatibility.
---
**Epic 1 Summary:** 8 stories, ~2-3 weeks. All FRs (FR-A, FR-B, FR-C5, C6) covered. All NFRs integrated (resilience, security, performance, observability).
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
