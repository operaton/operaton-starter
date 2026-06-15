---
baseline_commit:
---

# Story 2-1: Polish "?" Button Styling per UI Spec

## Status
ready-for-dev

## Story

As a **a developer exploring examples**,
I want **to see a clear "?" icon in the card header that toggles the detail view**,
So that **I can quickly expand a card to see full details without scrolling**.

## Acceptance Criteria

**Given** ExampleGalleryCard.vue has `detailsOpen` ref and toggle logic  
**When** the card is rendered  
**Then** a `<button>` element appears in the card header (top-right corner, inline with title)  
**And** the button displays a question mark icon (Unicode `?` or SVG glyph)  
**And** button size is 24×24px with minimum 24px touch target  
**And** button styling matches UI spec:
  - Default: transparent bg, neutral-500 text, border transparent
  - Hover: primary-blue text + border, rounded border, bg with 5% opacity
  - Focus: 2px outline with 2px offset
  - Expanded: rotated 180° (via `transform: rotate(180deg)`)  
**And** the button has `aria-expanded="true|false"` attribute (updates with state)  
**And** the button has `aria-controls="details-{example-id}"` (links to detail panel ID)  
**And** the button has `title="Show details"` tooltip  
**And** keyboard interaction works:
  - Enter/Space toggles expanded state
  - Escape (when focus is in expanded details panel) collapses details  
**And** the button is only rendered if `hasDetails` is true (i.e., there's detail content to show)  
**Unit test:** Render card with details; click "?"; verify expanded state; verify rotation CSS applied; test keyboard nav.
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
