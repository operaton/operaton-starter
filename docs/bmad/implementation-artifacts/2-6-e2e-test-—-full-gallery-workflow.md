---
baseline_commit:
---

# Story 2-6: E2E Test — Full Gallery Workflow

## Status
ready-for-dev

## Story

As a **a product owner validating the gallery feature**,
I want **to verify the complete user workflow: browse → search → filter → expand → view details → download**,
So that **the gallery is production-ready and users can discover and access examples seamlessly**.

## Acceptance Criteria

**Given** the app is running with loaded examples (from Epic 1)  
**When** a user navigates to the Gallery page  
**Then** the Examples section is visible with:
  - Section title "Examples" and blurb ("Real-world runnable examples...")
  - GallerySearchBar with search input + runtime/buildSystem/complexity/integrations filters
  - Grid of ExampleGalleryCard components (responsive: 1/2/3 columns per breakpoint)
  - ExamplesEmptyState hidden (examples exist)  
**When** the user types "kafka" in search  
**Then** the list filters to examples matching "kafka" (in title, description, tags, or integrations)  
**When** the user clicks complexity filter "intermediate"  
**Then** examples are further filtered (kafka + intermediate)  
**When** the user clicks a "?" button on a card  
**Then** the detail panel expands, showing:
  - Long description (rendered markdown)
  - BPMN concepts (as tags)
  - Integrations (as tags)
  - Authors (as links if URLs provided)
  - License
  - Last updated date
  - Pinned commit SHA (7-char, monospace font)
  - Tags (same chips as card summary)  
**And** the "?" button rotates 180° to indicate expanded state  
**When** the user clicks the "Download" button  
**Then** the button shows spinner + "Downloading..." text  
**And** the browser downloads a ZIP file (filename: `{exampleId}.zip`)  
**When** the download completes  
**Then** the button returns to normal state ("Download")  
**And** the file is saved to the user's downloads folder  
**When** the user encounters a broken example (isDownloadable: false)  
**Then** the Download button is disabled (neutral-300 bg)  
**And** a ⚠️ warning icon appears with tooltip "Example not found. Report issue →"  
**And** clicking the icon opens the GitHub repo in a new tab  
**When** the user clears filters  
**Then** all examples are shown again  
**And** the search box is cleared  
**E2E test (Playwright/Cypress):** 
1. Navigate to gallery
2. Search for "kafka" → verify 2 examples shown
3. Click complexity filter → verify 1 example shown
4. Click "?" → verify details expand
5. Click Download → verify file downloads
6. Clear filters → verify all examples show again
---
**Epic 2 Summary:** 6 stories, ~1-2 weeks. All FRs (D1, D3-D5, D7, C1) covered. Frontend polish + integration testing.
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
