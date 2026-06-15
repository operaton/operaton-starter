---
baseline_commit:
---

# Story 2-4: Integration Test — Search + Filter Flow

## Status
ready-for-dev

## Story

As a **a frontend developer ensuring search works**,
I want **to verify that typing in the search box and toggling filter chips correctly narrows the examples list**,
So that **users can reliably find examples by searching and filtering**.

## Acceptance Criteria

**Given** GalleryView.vue with multiple examples (≥5 with different runtime, complexity, integrations)  
**When** a user types "kafka" in the search box  
**Then** `useGalleryFilters` filters examples by title, description, tags, and integrations  
**And** only examples matching the search query are displayed  
**When** a user clicks a runtime filter chip (e.g., "spring-boot")  
**Then** examples are further filtered to those matching the selected runtime  
**And** other runtime chips show as toggleable (clicking another runtime replaces the previous filter)  
**When** a user clicks multiple filter chips (e.g., complexity "intermediate" + integration "kafka")  
**Then** examples are filtered by ALL active filters (AND logic)  
**And** `hasActiveFilters` flag indicates filters are active  
**When** a user clears filters (via "Clear" button)  
**Then** all filters reset and all examples are shown  
**And** the search query clears  
**Example flow:**
1. User sees 12 examples
2. User types "saga" → sees 2 examples (saga in title/desc)
3. User clicks complexity "intermediate" → sees 1 example (saga + intermediate)
4. User clicks integration "kafka" → still sees 1 example (kafka-saga, intermediate)
5. User clicks "Clear" → sees all 12 examples again
**Integration test:** Mock metadata with 5+ diverse examples; test search, single filter, multi-filter, clear; verify DOM updates.
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
