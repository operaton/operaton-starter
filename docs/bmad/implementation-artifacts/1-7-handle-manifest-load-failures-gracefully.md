---
baseline_commit:
---

# Story 1-7: Handle Manifest Load Failures Gracefully

## Status
ready-for-dev

## Story

As a **a developer who depends on the gallery**,
I want **the app to start even if one example repository is broken**,
So that **I'm not blocked by a flaky external dependency**.

## Acceptance Criteria

**Given** configured repositories include at least one broken source (404, network timeout, parse error, invalid schema)  
**When** the ExampleRepositoryLoader initializes at startup  
**Then** failed sources are logged at WARN level with structured fields: `source`, `reason`, `exampleCount_before_failure`  
**And** failed sources do NOT prevent other sources from loading (failures are isolated)  
**And** the application continues to start normally (startup is never blocked by manifest load failures)  
**And** the ExampleRegistry remains in-memory with successfully loaded examples only  
**And** on manual refresh, if a source fails, the previous snapshot for that source is preserved (existing examples remain available)  
**And** logging includes enough detail for ops to diagnose the issue (e.g., "network_timeout", "manifest_parse_error", "unknown_apiVersion")  
**Log example:**
```
WARN o.o.d.s.examples.ExampleRepositoryLoader : Failed to load examples from source
  source=broken-repo/operaton-examples
  reason=manifest_parse_error
  error=Invalid YAML in .operaton-starter.yml: unexpected character
```
**Integration test:** Configure one valid source + one broken source; verify startup succeeds; verify valid examples load; verify failure is logged.
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
