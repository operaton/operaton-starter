---
baseline_commit:
---

# Story 1-2: Load Manifests from GitHub at Startup

## Status
ready-for-dev

## Story

As a **system operator**,
I want **manifests from registered repositories to be loaded automatically when the server starts**,
So that **examples are available immediately without manual intervention**.

## Acceptance Criteria

**Given** `starter.examples.repositories` Spring property configured (e.g., `starter.examples.repositories[0]=operaton/operaton-examples`)  
**When** the ExampleRepositoryLoader bean initializes at application startup  
**Then** for each registered repository, the loader fetches `.operaton-starter.yml` from GitHub raw content URL  
**And** fetches succeed within 5 seconds per source (hard timeout); sources exceeding timeout are skipped with a warning  
**And** total startup overhead is ≤ 3 seconds for N ≤ 10 sources (fetches run in parallel, not sequentially)  
**And** the in-memory ExampleRegistry snapshot is populated with all successfully loaded examples  
**And** if all sources fail to load, the registry is empty but startup is NOT blocked  
**Integration test:** Mock GitHub endpoint; verify parallel fetches; measure startup latency; verify in-memory snapshot populated.
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
