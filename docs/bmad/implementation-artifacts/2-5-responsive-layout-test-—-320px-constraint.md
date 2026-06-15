---
baseline_commit:
---

# Story 2-5: Responsive Layout Test — 320px Constraint

## Status
ready-for-dev

## Story

As a **a mobile user browsing examples on an iPhone SE**,
I want **the card layout to adapt gracefully to a 320px viewport**,
So that **I can browse and interact with cards without horizontal scrolling or cramped text**.

## Acceptance Criteria

**Given** ExampleGalleryCard.vue and GalleryView.vue with responsive CSS  
**When** rendered at 320px viewport width  
**Then** card layout adapts:
  - Icon size: 40×40px (vs. 48×48px at desktop)
  - Card title: font-size 0.875rem (vs. 1rem), may wrap to 2 lines
  - Short description: font-size 0.75rem, full width, may wrap
  - Tags: flex-wrap wrap, gap 0.5rem
  - Metadata badges: font-size 0.625rem  
**And** action row (Download + View on GitHub) adapts:
  - Option A (preferred): Stack vertically with full width (100% each)
  - Option B: Shrink font to 0.875rem, keep inline
  - Recommendation: Choose Option A for less cramped feel  
**And** grid layout adapts:
  - 320px: 1 column (single card per row)
  - 375px+: Stay 1 column until 640px breakpoint  
  - 640px+: 2 columns  
  - 768px+: 3 columns  
**And** no text truncation surprises (titles wrap naturally, not cut off)  
**And** no horizontal scroll on the card or page  
**And** touch targets remain ≥24px (buttons, icons)  
**And** the "?" button and ⚠️ icon are still clickable and clearly visible  
**Visual regression test:** Render card at 320px, 375px, 640px, 768px; screenshot compare; verify no overflow.
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
