---
baseline_commit:
---

# Story 2-2: Add ⚠️ Warning Icon to Download Action

## Status
ready-for-dev

## Story

As a **a developer encountering a broken example**,
I want **to see a warning icon when an example is missing or unavailable**,
So that **I can click it to report the issue to the maintainer**.

## Acceptance Criteria

**Given** DownloadAction.vue receives `downloadStatus` prop with `isAvailable` field  
**When** `isAvailable === false` (example missing at pinned SHA)  
**Then** the Download button is disabled and styled:
  - Background: neutral-300
  - Text: neutral-400
  - Cursor: not-allowed  
**And** a warning icon (⚠️ emoji or SVG triangle) appears to the right of the Download button  
**And** the warning icon is styled per UI spec:
  - Size: 20×20px
  - Color: amber-600
  - Interactive: clickable `<button>` or `<a>` element  
  - Hover: 10% amber background, amber border  
  - Focus: 2px amber outline with 2px offset  
**And** clicking the warning icon opens `example.sourceRepoUrl` in a new tab (`target="_blank" rel="noopener noreferrer"`)  
**And** the warning icon has `title="Example not found in source repo. Report issue →"` tooltip  
**And** when `isAvailable === true`, the warning icon is hidden (no extra space)  
**And** download button remains clickable and spinner shows during download  
**Integration test:** Mock isAvailable true/false; verify button disabled state; verify icon appears/disappears; verify click opens repo link.
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
