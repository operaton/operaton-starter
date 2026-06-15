---
baseline_commit:
---

# Story 3-3: Implement Download Telemetry Endpoint

## Status
ready-for-dev

## Story

As a **a product analyst tracking example adoption**,
I want **to see how many times each example has been downloaded**,
So that **I can identify which examples are most valuable to the community**.

## Acceptance Criteria

**Given** users download examples via the download endpoint  
**When** each download completes (success only, not failures)  
**Then** the endpoint increments an in-process counter per `(sourceRepo, exampleId)`  
**And** counter format: `Map<String, Map<String, Long>>` → `{sourceRepo: {exampleId: count, ...}, ...}`  
**And** counters are NOT persisted to disk (reset on server restart)  
**When** a request is made to `GET /actuator/examples` (via Spring Boot Actuator)  
**Then** the endpoint returns a snapshot of current download counters:
```json
{
  "timestamp": "2026-06-15T14:30:00Z",
  "downloads": {
    "kthoms/operaton-examples": {
      "kafka-saga": 42,
      "dmn-rules": 15,
      "rest-service": 8
    },
    "acme/operaton-samples": {
      "payment-approval": 3
    }
  }
}
```
**And** the endpoint logs no PII (just counts)  
**And** no remote reporting (counts stay server-side only)  
**Unit test:** Call download endpoint multiple times; call `/actuator/examples`; verify counters incremented correctly.
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
