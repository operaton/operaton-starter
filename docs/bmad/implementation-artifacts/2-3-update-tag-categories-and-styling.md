---
baseline_commit:
---

# Story 2-3: Update Tag Categories & Styling

## Status
ready-for-dev

## Story

As a **a developer filtering by integration type**,
I want **tags to render with appropriate colors based on their category**,
So that **I can quickly scan and identify examples by technology (Kafka, REST, BPMN, etc.)**.

## Acceptance Criteria

**Given** new tag categories in the PRD: RUNTIME, BUILD_SYSTEM, COMPLEXITY, INTEGRATION, CONCEPT  
**When** ExampleGalleryCard.vue renders tags  
**Then** `tagColors.ts` is updated to support all categories:
```typescript
export function tagChipClasses(category: TagCategory): string {
  switch (category) {
    case 'BPMN_CONCEPT':
    case 'CONCEPT':
      return 'bg-blue-100 text-blue-800'
    case 'TECHNOLOGY':
    case 'INTEGRATION':
      return 'bg-amber-100 text-amber-800'
    case 'PLATFORM':
      return 'bg-green-100 text-green-800'
    case 'STANDARD':
      return 'bg-purple-100 text-purple-800'
    default:
      return 'bg-neutral-100 text-neutral-600'
  }
}
export function metadataBadgeClasses(category: TagCategory): string {
  if (['RUNTIME', 'BUILD_SYSTEM', 'COMPLEXITY'].includes(category)) {
    return 'bg-neutral-50 text-neutral-900 border border-neutral-200'
  }
  return 'bg-neutral-50 text-neutral-900 border border-neutral-200'
}
```
**And** on the card, tags render using the appropriate function:
  - RUNTIME, BUILD_SYSTEM, COMPLEXITY → `metadataBadgeClasses()` (monochrome badges)
  - BPMN_CONCEPT, CONCEPT, TECHNOLOGY, INTEGRATION, PLATFORM, STANDARD → `tagChipClasses()` (colored chips)  
**And** in the expanded detail view, tags are rendered the same way (colored chips for flavor tags, monochrome badges for metadata)  
**And** tag colors meet WCAG AA contrast ratio (4.5:1 for text on background)  
**Unit test:** Render card with mixed tag categories; verify correct colors applied; verify contrast ratios.
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
