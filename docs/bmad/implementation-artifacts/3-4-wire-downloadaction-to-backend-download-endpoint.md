---
baseline_commit:
---

# Story 3-4: Wire DownloadAction to Backend Download Endpoint

## Status
ready-for-dev

## Story

As a **a frontend developer integrating the download button**,
I want **the Download button to call the backend endpoint and handle the response**,
So that **users can seamlessly download examples without seeing network details**.

## Acceptance Criteria

**Given** DownloadAction.vue receives `example` prop and `downloadStatus` prop  
**When** the user clicks the Download button  
**Then** `useExampleDownload` composable calls `GET /api/v1/examples/{owner}/{repo}/{exampleId}/download`  
**And** sets `downloadStatus.isLoading = true` (triggers spinner)  
**And** while loading, the button shows "Downloading..." + spinner icon  
**When** the response succeeds (200 OK)  
**Then** the browser's native download behavior triggers (blob download)  
**And** `downloadStatus.isLoading = false`  
**And** the button returns to "Download" state  
**And** a success toast shows: "Downloaded {exampleId}.zip" (auto-dismiss in 3 seconds)  
**When** the response fails (404, 502, 413 or network timeout)  
**Then** `downloadStatus.error = error message` (one of: "Example not found", "GitHub unavailable", "Example too large", "Network error")  
**And** `downloadStatus.isLoading = false`  
**And** the button returns to "Download" state (clickable for retry)  
**And** an error toast shows with Retry button (see Story 3.5)  
**When** the user clicks the Retry button in the error toast  
**Then** the download is attempted again  
**Unit test (Vitest/Jsdom):** Mock fetch; test success (200), 404, 502, timeout; verify state updates; verify Retry works.
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
