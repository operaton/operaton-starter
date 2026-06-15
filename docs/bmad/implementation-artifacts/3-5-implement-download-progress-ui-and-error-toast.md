---
baseline_commit:
---

# Story 3-5: Implement Download Progress UI & Error Toast

## Status
ready-for-dev

## Story

As a **a developer experiencing a download failure**,
I want **to see a clear error message and have the option to retry**,
So that **I can recover from transient network issues without giving up**.

## Acceptance Criteria

**Given** a download fails (404, 502, 413, timeout)  
**When** the error is received  
**Then** an error toast appears at bottom-left of viewport with:
  - Error message (per UI spec): "Example not found" / "GitHub unavailable" / "Example too large (>50 MB)" / "Download timed out"
  - Dismiss button (X or "Dismiss")
  - Retry button  
**And** the toast styling per UI spec:
  - Background: rgb(239, 68, 68) (red-600)
  - Text: white
  - Position: bottom-left (mobile-friendly; not bottom-right which hides on small screens)
  - Duration: 6 seconds auto-dismiss OR manual dismiss  
**When** the user clicks Retry  
**Then** the same download is attempted again (reuses the same example/endpoint)  
**And** the loading spinner shows again  
**And** the error toast is replaced  
**When** the download succeeds on retry  
**Then** the file downloads and success toast shows (as per Story 3.4)  
**When** the download fails again on retry  
**Then** the error toast reappears (same format)  
**Toast messages (exact per FR-D6):**
- 404: "Example not found in source repo. Report issue."
- 502: "GitHub unreachable. Try again?"
- 413: "Example is too large to download (exceeds 50 MB)."
- Timeout: "Download took too long. Try again?"
**Component integration:** Toast component lives in GalleryView or global layout; useExampleDownload emits error events; toast listens and displays.
**Visual regression test:** Trigger each error type; screenshot toast styling; verify position, colors, buttons.
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
