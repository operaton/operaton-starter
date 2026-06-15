---
baseline_commit:
---

# Story 4-2: Add "Publish Your Own Examples" Link to Gallery UI

## Status
ready-for-dev

## Story

As a **user browsing the gallery**,
I want **an obvious way to learn how to contribute my own examples**,
So that **I'm encouraged to share examples without searching the docs**.

## Acceptance Criteria

**Given** the gallery view displays examples  
**When** I scroll to the empty state OR the bottom of the gallery  
**Then** I see a call-to-action card or link section with:
**Card text (Recommended):**
```
Publishing Your Own Examples
Have an Operaton pattern or workflow you'd like to share?
[Learn how to contribute →]
```
**OR inline link (if less intrusive):**
```
Can't find what you need? Learn how to publish your own examples →
```
**Behavior:**
- Link href: `/docs/examples-repository-format.md` (or external URL if published online)
- Text color: primary-blue (consistent with other gallery links)
- Hover state: underline + blue highlight
- Mobile (320px): Text is full-width, link is tap-friendly (≥44px height)
**Styling:**
- If card: neutral-50 bg with border, 1rem padding, centered text, 8–12px gap between text and button
- If inline: inherit parent typography, no special styling
**Placement:** 
- Below the gallery grid OR at the bottom of the empty state message (whichever is rendered)
- No overlap with other UI elements
**A/B criteria:** Track clicks via telemetry (optional). Success: ≥1 user navigates to contributing docs per week (baseline after launch).
**Implementation note:** Vue component ExampleGalleryView or empty-state sub-component
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
