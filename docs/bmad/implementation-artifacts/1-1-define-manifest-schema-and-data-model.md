---
baseline_commit:
---

# Story 1-1: Define Manifest Schema & Data Model

## Status
ready-for-dev

## Story

As a **backend engineer**,
I want **to define the Example entity and manifest schema**,
So that **all subsequent loader stories have a clear data contract**.

## Acceptance Criteria

**Given** the PRD specifies manifest structure (FR-A1 through A7)  
**When** I create the Example domain model and manifest parser  
**Then** the Example class includes all required fields: `id`, `title`, `shortDescription`, `path` (mandatory) plus all optional fields (longDescription, tags, buildSystem, runtime, icon, etc.)  
**And** unknown manifest fields are ignored without error (forward-compatibility)  
**And** `apiVersion: operaton-starter/v1` is parsed; manifests with unknown major versions log a warning but don't fail parsing  
**And** manifest YAML larger than 256 KB is rejected with a logged warning  
**Example model structure:**
```java
public record Example(
  String id,
  String title,
  String shortDescription,
  String path,
  String owner,
  String repo,
  String sourceRepoSha,
  // ... all optional fields
) {}
```
**Unit test:** Parse sample `.operaton-starter.yml` (from PRD appendix); verify all fields hydrate correctly; verify unknown fields don't throw exceptions.
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
