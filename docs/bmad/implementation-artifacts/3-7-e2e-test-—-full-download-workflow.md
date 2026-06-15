---
baseline_commit:
---

# Story 3-7: E2E Test — Full Download Workflow

## Status
ready-for-dev

## Story

As a **a product owner verifying the download feature**,
I want **to verify the complete download workflow from button click to file save**,
So that **users can reliably download and extract examples**.

## Acceptance Criteria

**Given** the app is running with examples loaded and the download endpoint working  
**When** a user navigates to an example card  
**Then** the Download button is visible (enabled or disabled based on `isDownloadable`)  
**When** `isDownloadable === true` (example exists)  
**Then** the Download button is enabled and clickable  
**When** the user clicks Download  
**Then** the button shows "Downloading..." + spinner  
**And** the API call is made to the download endpoint  
**When** the download completes successfully  
**Then** the browser's native download dialog appears  
**And** the file `{exampleId}.zip` is saved to the user's Downloads folder  
**And** a success toast shows: "Downloaded {exampleId}.zip" (optional, auto-dismisses)  
**And** the button returns to "Download" state  
**When** the download fails (e.g., GitHub unreachable)  
**Then** an error toast shows with Retry button  
**And** clicking Retry attempts the download again  
**When** `isDownloadable === false` (example missing at pinned SHA)  
**Then** the Download button is disabled (neutral-300 bg, neutral-400 text)  
**And** a ⚠️ warning icon appears with tooltip "Example not found. Report issue →"  
**And** clicking the icon opens the GitHub repo in a new tab  
**Full flow test (Playwright):**
1. Load gallery with mixed examples (available + unavailable)
2. Click Download on available example → verify file downloads
3. Click Download on unavailable example → verify button disabled, ⚠️ icon present
4. Trigger network error (mock GitHub down) → verify error toast + Retry button
5. Click Retry → verify re-attempt works
6. Verify downloaded file can be extracted (optional, higher-level validation)
---
**Epic 3 Summary:** 7 stories, ~1.5-2 weeks. All FRs (D2, D6, C2-C4, C7-C8) covered. Backend download pipeline + frontend integration + comprehensive testing.
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
